package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.domain.performance.api.KopisPerformanceClient;
import com.example.bookwithticket.domain.performance.api.KopisPerformanceResponse;
import com.example.bookwithticket.domain.reservation.Seat;
import com.example.bookwithticket.domain.reservation.SeatRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KopisPerformanceImportService {

    private final KopisPerformanceClient kopisClient;
    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final com.example.bookwithticket.domain.reservation.ReservationRepository reservationRepository;
    private final com.example.bookwithticket.cart.repository.PerformanceCartItemRepository performanceCartItemRepository;

    public KopisPerformanceImportService(
            KopisPerformanceClient kopisClient,
            PerformanceRepository performanceRepository,
            PerformanceScheduleRepository scheduleRepository,
            SeatRepository seatRepository,
            com.example.bookwithticket.domain.reservation.ReservationRepository reservationRepository,
            com.example.bookwithticket.cart.repository.PerformanceCartItemRepository performanceCartItemRepository
    ) {
        this.kopisClient = kopisClient;
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.performanceCartItemRepository = performanceCartItemRepository;
    }

    public List<KopisPerformanceResponse.Item> searchKopis(String keyword) {
        return kopisClient.search(keyword, 20);
    }

    @Transactional
    public List<PerformanceResponse> importSelectedItems(List<KopisPerformanceResponse.Item> selectedItems) {
        List<PerformanceResponse> importedList = new ArrayList<>();

        if (selectedItems == null || selectedItems.isEmpty()) {
            return importedList;
        }

        for (KopisPerformanceResponse.Item item : selectedItems) {
            boolean exists = performanceRepository.findAllByOrderByIdDesc().stream()
                    .anyMatch(p -> p.getTitle().equals(item.getPrfnm()));
            if (exists) {
                continue;
            }

            // KOPIS 공연시설 상세 API(/prfplc)에서 수신한 진짜 객석수 (없을 시 100석)
            int actualSeatScale = (item.getSeatscale() != null && item.getSeatscale() > 0) ? item.getSeatscale() : 100;

            int runtime = parseRuntime(item.getPrfruntime());
            String description = buildDescription(item, actualSeatScale);

            PerformanceCategory category = parseCategory(item.getGenrenm());
            Performance p = new Performance(
                    item.getPrfnm(),
                    category,
                    item.getFcltynm() != null ? item.getFcltynm() : "주요 공연장",
                    item.getPoster(),
                    runtime,
                    description,
                    null
            );
            p.setSeatscale(actualSeatScale);

            Performance savedPerformance = performanceRepository.save(p);

            // KOPIS 실제 공연기간 (prfpdfrom ~ prfpdto) 및 요일 안내(dtguidance) 기반 동적 회차 산출
            LocalDate startDate = parseKopisDate(item.getPrfpdfrom());
            LocalDate endDate = parseKopisEndDate(item.getPrfpdto(), startDate);

            int hour = parseKopisHour(item.getDtguidance());
            int minute = parseKopisMinute(item.getDtguidance());

            // dtguidance 요일 일치 검증을 거쳐 실제 공연 요일에 해당하는 날짜만 수집 (최대 5개 회차)
            List<LocalDate> validDays = new ArrayList<>();
            LocalDate curr = startDate;
            while (!curr.isAfter(endDate) && validDays.size() < 5) {
                if (matchesDayOfWeek(curr, item.getDtguidance())) {
                    validDays.add(curr);
                }
                curr = curr.plusDays(1);
            }
            // 특정 요일 매칭이 없거나 파싱 실패 시 기본 시작일로부터 순차 생성
            if (validDays.isEmpty()) {
                long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                int countToCreate = (int) Math.min(Math.max(1, totalDays), 5);
                for (int k = 0; k < countToCreate; k++) {
                    validDays.add(startDate.plusDays(k));
                }
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime openTimePast = now.minusHours(1); // 오픈 완료 (바로 예매 가능)
            LocalDateTime openTimeFuture = now.plusHours(24); // 오픈 예정 테스트용 (마지막 회차: 현재시각 + 24시간 후)

            int totalCreated = 0;
            for (int k = 0; k < validDays.size(); k++) {
                LocalDate day = validDays.get(k);
                // 해당 요일에 명시된 모든 시간대 추출 (예: "토요일(14:00,17:00)" ➔ 14:00, 17:00)
                List<int[]> times = parseTimesForDay(day, item.getDtguidance(), hour, minute);
                for (int[] time : times) {
                    LocalDateTime perfTime = day.atTime(time[0], time[1]);

                    // 마지막 회차는 오픈 예정 테스트 (현재시간 + 24시간 후 오픈)
                    LocalDateTime openTime = (k == validDays.size() - 1 && times.indexOf(time) == times.size() - 1 && totalCreated > 0) ? openTimeFuture : openTimePast;

                    PerformanceSchedule s = new PerformanceSchedule(savedPerformance, perfTime, openTime);
                    scheduleRepository.save(s);

                    // KOPIS 실제 수용 좌석 수 100% 동일 생성 (64개 단위 섹터, 8x8 배치)
                    generateExactKopisSeats(s, actualSeatScale);
                    totalCreated++;
                }
            }

            importedList.add(PerformanceResponse.from(savedPerformance));
        }

        return importedList;
    }

    @Transactional
    public int resyncAllPerformancesSchedules() {
        List<Performance> performances = performanceRepository.findAll();
        int updatedCount = 0;
        for (Performance p : performances) {
            String desc = p.getDescription();
            if (desc == null || desc.isBlank()) continue;

            String guidance = extractMetaValue(desc, "• 공연시간:");
            if (guidance == null || guidance.isBlank()) continue;

            String period = extractMetaValue(desc, "• 공연기간:");
            List<PerformanceSchedule> oldSchedules = scheduleRepository.findByPerformanceIdOrderByPerformanceTimeAsc(p.getId());

            LocalDate startDate;
            LocalDate endDate;

            if (period != null && !period.isBlank() && period.contains("~")) {
                String[] parts = period.split("~");
                startDate = parseKopisDate(parts[0].trim());
                endDate = parseKopisEndDate(parts[1].trim(), startDate);
            } else if (!oldSchedules.isEmpty()) {
                // 기존 회차 일시 범위 활용 (최소일부터 21일간)
                startDate = oldSchedules.get(0).getPerformanceTime().toLocalDate();
                endDate = startDate.plusDays(21);
            } else {
                startDate = LocalDate.now().plusDays(2);
                endDate = startDate.plusDays(21);
            }

            int hour = parseKopisHour(guidance);
            int minute = parseKopisMinute(guidance);

            List<LocalDate> validDays = new ArrayList<>();
            LocalDate curr = startDate;
            while (!curr.isAfter(endDate) && validDays.size() < 10) {
                if (matchesDayOfWeek(curr, guidance)) {
                    validDays.add(curr);
                }
                curr = curr.plusDays(1);
            }
            if (validDays.isEmpty()) {
                long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                int countToCreate = (int) Math.min(Math.max(1, totalDays), 5);
                for (int k = 0; k < countToCreate; k++) {
                    validDays.add(startDate.plusDays(k));
                }
            }

            // 기존 회차 및 좌석 삭제 전 연관된 예매 및 장바구니 데이터 먼저 정리 (외래키 무결성 보장)
            for (PerformanceSchedule oldSch : oldSchedules) {
                reservationRepository.deleteByScheduleId(oldSch.getId());
                performanceCartItemRepository.deleteByPerformanceScheduleId(oldSch.getId());
                seatRepository.deleteByScheduleId(oldSch.getId());
                scheduleRepository.delete(oldSch);
            }

            int scale = p.getSeatscale() > 0 ? p.getSeatscale() : 100;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime openPast = now.minusHours(1);

            for (LocalDate day : validDays) {
                List<int[]> times = parseTimesForDay(day, guidance, hour, minute);
                for (int[] t : times) {
                    LocalDateTime perfTime = day.atTime(t[0], t[1]);
                    PerformanceSchedule s = new PerformanceSchedule(p, perfTime, openPast);
                    scheduleRepository.save(s);
                    generateExactKopisSeats(s, scale);
                }
            }
            updatedCount++;
        }
        return updatedCount;
    }

    private String extractMetaValue(String description, String prefix) {
        if (description == null) return null;
        int idx = description.indexOf(prefix);
        if (idx == -1) return null;
        int start = idx + prefix.length();
        int end = description.indexOf("\n", start);
        if (end == -1) end = description.length();
        return description.substring(start, end).trim();
    }

    private List<int[]> parseTimesForDay(LocalDate date, String guidance, int defaultHour, int defaultMinute) {
        List<int[]> times = new ArrayList<>();
        if (guidance == null || guidance.isBlank()) {
            times.add(new int[]{defaultHour, defaultMinute});
            return times;
        }
        java.time.DayOfWeek dow = date.getDayOfWeek();
        String dayPatternStr = switch (dow) {
            case MONDAY -> "(월요일|(?<![가-힣])월)";
            case TUESDAY -> "(화요일|(?<![가-힣])화)";
            case WEDNESDAY -> "(수요일|(?<![가-힣])수)";
            case THURSDAY -> "(목요일|(?<![가-힣])목)";
            case FRIDAY -> "(금요일|(?<![가-힣])금)";
            case SATURDAY -> "(토요일|(?<![가-힣])토)";
            case SUNDAY -> "(일요일|(?<![가-힣])일)";
        };

        try {
            // 해당 요일 괄호 안의 모든 시간 추출 (예: "토요일(14:00,17:00)", "일요일(15:00)")
            Pattern dayBlockPattern = Pattern.compile(dayPatternStr + "[^0-9\\(]{0,10}\\(([^\\)]+)\\)");
            Matcher blockMatcher = dayBlockPattern.matcher(guidance);
            if (blockMatcher.find()) {
                String innerTimes = blockMatcher.group(2);
                Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
                Matcher tm = timePattern.matcher(innerTimes);
                while (tm.find()) {
                    int h = Integer.parseInt(tm.group(1));
                    int m = Integer.parseInt(tm.group(2));
                    if (h >= 0 && h <= 23 && m >= 0 && m <= 59) {
                        times.add(new int[]{h, m});
                    }
                }
            }
        } catch (Exception ignored) {}

        if (times.isEmpty()) {
            // fallback: 단일 시간 검색
            try {
                Pattern dayTimePattern = Pattern.compile(dayPatternStr + "[^0-9\\(]{0,10}\\(?\\s*(\\d{1,2}):(\\d{2})");
                Matcher matcher = dayTimePattern.matcher(guidance);
                if (matcher.find()) {
                    int h = Integer.parseInt(matcher.group(2));
                    int m = Integer.parseInt(matcher.group(3));
                    if (h >= 0 && h <= 23 && m >= 0 && m <= 59) {
                        times.add(new int[]{h, m});
                    }
                }
            } catch (Exception ignored) {}
        }

        if (times.isEmpty()) {
            times.add(new int[]{defaultHour, defaultMinute});
        }
        return times;
    }

    private boolean matchesDayOfWeek(LocalDate date, String guidance) {
        if (guidance == null || guidance.isBlank()) return true;
        java.time.DayOfWeek dow = date.getDayOfWeek();

        String dayFull = switch (dow) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
        String dayShort = dayFull.substring(0, 1);

        // 1. [휴무/제외일 우선 차단] "월요일 공연 없음", "월요일 휴관", "월요일 제외" 등 부정 패턴 감지 시 즉시 제외
        Pattern restPattern = Pattern.compile("(" + dayFull + "|" + dayShort + ")\\s*(요일)?\\s*(공연\\s*없음|휴관|제외|쉼|미진행|휴무)");
        if (restPattern.matcher(guidance).find()) {
            return false;
        }

        // 2. 범위 요일 안내 검사
        if (guidance.contains("월요일 ~ 금요일") || guidance.contains("월~금") || guidance.contains("평일")) {
            if (dow.getValue() >= 1 && dow.getValue() <= 5) return true;
        }
        if (guidance.contains("화요일 ~ 일요일") || guidance.contains("화~일")) {
            if (dow.getValue() >= 2 && dow.getValue() <= 7) return true;
        }
        if (guidance.contains("토요일 ~ 일요일") || guidance.contains("토~일") || guidance.contains("주말")) {
            if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) return true;
        }

        // 3. 단일 요일 및 시간대 지정 안내 검사 (예: "토요일(14:00)", "일(15:00)")
        String dayPatternStr = switch (dow) {
            case MONDAY -> "(월요일|(?<![가-힣])월\\s*\\()";
            case TUESDAY -> "(화요일|(?<![가-힣])화\\s*\\()";
            case WEDNESDAY -> "(수요일|(?<![가-힣])수\\s*\\()";
            case THURSDAY -> "(목요일|(?<![가-힣])목\\s*\\()";
            case FRIDAY -> "(금요일|(?<![가-힣])금\\s*\\()";
            case SATURDAY -> "(토요일|(?<![가-힣])토\\s*\\()";
            case SUNDAY -> "(일요일|(?<![가-힣])일\\s*\\()";
        };
        Pattern validDayPattern = Pattern.compile(dayPatternStr);
        return validDayPattern.matcher(guidance).find();
    }

    private LocalDate parseKopisDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDate.now().plusDays(3);
        try {
            String cleaned = dateStr.replaceAll("[^0-9]", "");
            if (cleaned.length() >= 8) {
                LocalDate date = LocalDate.parse(cleaned.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.isBefore(LocalDate.now()) ? LocalDate.now().plusDays(2) : date;
            }
        } catch (Exception ignored) {}
        return LocalDate.now().plusDays(3);
    }

    private LocalDate parseKopisEndDate(String dateStr, LocalDate startDate) {
        if (dateStr == null || dateStr.isBlank()) return startDate.plusDays(4);
        try {
            String cleaned = dateStr.replaceAll("[^0-9]", "");
            if (cleaned.length() >= 8) {
                LocalDate date = LocalDate.parse(cleaned.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.isBefore(startDate) ? startDate : date;
            }
        } catch (Exception ignored) {}
        return startDate.plusDays(4);
    }

    private int parseKopisHour(String guidance) {
        if (guidance == null || guidance.isBlank()) return 19;
        try {
            Pattern pattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
            Matcher matcher = pattern.matcher(guidance);
            if (matcher.find()) {
                int h = Integer.parseInt(matcher.group(1));
                if (h >= 0 && h <= 23) return h;
            }
        } catch (Exception ignored) {}
        return 19;
    }

    private int parseKopisMinute(String guidance) {
        if (guidance == null || guidance.isBlank()) return 30;
        try {
            Pattern pattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
            Matcher matcher = pattern.matcher(guidance);
            if (matcher.find()) {
                int m = Integer.parseInt(matcher.group(2));
                if (m >= 0 && m <= 59) return m;
            }
        } catch (Exception ignored) {}
        return 30;
    }

    private void generateExactKopisSeats(PerformanceSchedule schedule, int totalSeats) {
        List<Seat> seatList = new ArrayList<>();
        int vipCount = Math.max(1, (int) Math.round(totalSeats * 0.10));
        int rCount = Math.max(1, (int) Math.round(totalSeats * 0.10));
        int sCount = Math.max(1, (int) Math.round(totalSeats * 0.40));

        for (int i = 1; i <= totalSeats; i++) {
            int sectorNum = ((i - 1) / 64) + 1;
            int startSeat = (sectorNum - 1) * 64 + 1;
            int endSeat = Math.min(sectorNum * 64, totalSeats);
            String sectorName = String.format("%d섹터 (%d~%d번)", sectorNum, startSeat, endSeat);

            String tier;
            int price;
            if (i <= vipCount) {
                tier = "VIP";
                price = 180000;
            } else if (i <= vipCount + rCount) {
                tier = "R";
                price = 150000;
            } else if (i <= vipCount + rCount + sCount) {
                tier = "S";
                price = 100000;
            } else {
                tier = "A";
                price = 70000;
            }

            String seatNumber = String.format("[%s] %s-%d번", sectorName, tier, i);
            seatList.add(new Seat(schedule, seatNumber, price));
        }
        seatRepository.saveAll(seatList);
    }

    private PerformanceCategory parseCategory(String genreName) {
        if (genreName == null) return PerformanceCategory.MUSICAL;
        String upper = genreName.toUpperCase();
        if (upper.contains("콘서트") || upper.contains("CONCERT")) return PerformanceCategory.CONCERT;
        if (upper.contains("클래식") || upper.contains("CLASSIC")) return PerformanceCategory.CLASSIC;
        if (upper.contains("전시") || upper.contains("EXHIBITION")) return PerformanceCategory.EXHIBITION;
        return PerformanceCategory.MUSICAL;
    }

    private int parseRuntime(String runtimeStr) {
        if (runtimeStr == null || runtimeStr.isBlank()) return 120;
        try {
            int totalMinutes = 0;
            // 1. "X시간" 패턴 검색 (예: 1시간 ➔ 60분)
            Pattern hourPattern = Pattern.compile("(\\d+)\\s*시간");
            Matcher hm = hourPattern.matcher(runtimeStr);
            if (hm.find()) {
                totalMinutes += Integer.parseInt(hm.group(1)) * 60;
            }

            // 2. "Y분" 패턴 검색
            Pattern minPattern = Pattern.compile("(\\d+)\\s*분");
            Matcher mm = minPattern.matcher(runtimeStr);
            if (mm.find()) {
                totalMinutes += Integer.parseInt(mm.group(1));
            }

            // 3. 단위 없이 숫자만 기재된 경우 (예: "90")
            if (totalMinutes == 0) {
                String cleaned = runtimeStr.replaceAll("[^0-9]", "");
                if (!cleaned.isEmpty()) {
                    totalMinutes = Integer.parseInt(cleaned);
                }
            }

            if (totalMinutes > 0 && totalMinutes <= 600) {
                return totalMinutes;
            }
        } catch (Exception ignored) {}
        return 120;
    }

    private String buildDescription(KopisPerformanceResponse.Item item, int seatScale) {
        StringBuilder sb = new StringBuilder();

        // 1. 실제 줄거리/시놉시스가 있는 경우
        if (item.getSty() != null && !item.getSty().isBlank()) {
            sb.append(item.getSty().trim());
        } else {
            sb.append(item.getPrfnm()).append(" 공식 공연 안내입니다.");
        }

        // 2. 부가 공연 메타 정보 깔끔하게 추가
        List<String> metaList = new ArrayList<>();
        if (item.getPrfpdfrom() != null && item.getPrfpdto() != null && !item.getPrfpdfrom().isBlank()) {
            metaList.add("• 공연기간: " + item.getPrfpdfrom().trim() + " ~ " + item.getPrfpdto().trim());
        }
        if (item.getPrfcast() != null && !item.getPrfcast().isBlank()) {
            metaList.add("• 출연진: " + item.getPrfcast().trim());
        }
        if (item.getPcseguidance() != null && !item.getPcseguidance().isBlank()) {
            metaList.add("• 티켓가격: " + item.getPcseguidance().trim());
        }
        if (item.getPrfage() != null && !item.getPrfage().isBlank()) {
            metaList.add("• 관람연령: " + item.getPrfage().trim());
        }
        if (item.getDtguidance() != null && !item.getDtguidance().isBlank()) {
            metaList.add("• 공연시간: " + item.getDtguidance().trim());
        }
        if (seatScale > 0) {
            metaList.add("• 객석규모: " + seatScale + "석");
        }

        if (!metaList.isEmpty()) {
            sb.append("\n\n[공연 상세 정보]\n").append(String.join("\n", metaList));
        }

        return sb.toString();
    }
}
