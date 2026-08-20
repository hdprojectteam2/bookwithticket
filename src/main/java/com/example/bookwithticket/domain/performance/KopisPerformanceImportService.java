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

    public KopisPerformanceImportService(
            KopisPerformanceClient kopisClient,
            PerformanceRepository performanceRepository,
            PerformanceScheduleRepository scheduleRepository,
            SeatRepository seatRepository
    ) {
        this.kopisClient = kopisClient;
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
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

            PerformanceCategory category = parseCategory(item.getGenrenm());
            Performance p = new Performance(
                    item.getPrfnm(),
                    category,
                    item.getFcltynm() != null ? item.getFcltynm() : "주요 공연장",
                    item.getPoster(),
                    150,
                    "KOPIS 수집 공연 (실제 수용 좌석 수: " + actualSeatScale + "석, 시간안내: " + (item.getDtguidance() != null ? item.getDtguidance() : "19:30") + "): " + item.getPrfnm(),
                    null
            );
            p.setSeatscale(actualSeatScale);

            Performance savedPerformance = performanceRepository.save(p);

            // KOPIS 실제 공연기간 (prfpdfrom ~ prfpdto) 기반 동적 회차 산출
            LocalDate startDate = parseKopisDate(item.getPrfpdfrom());
            LocalDate endDate = parseKopisEndDate(item.getPrfpdto(), startDate);

            long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            int countToCreate = (int) Math.min(Math.max(1, totalDays), 5); // 1일 공연이면 1개 회차만, 최대 5개까지만 실제 기간 내 동적 생성

            int hour = parseKopisHour(item.getDtguidance());
            int minute = parseKopisMinute(item.getDtguidance());

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime openTimePast = now.minusHours(1); // 오픈 완료 (바로 예매 가능)
            LocalDateTime openTimeFuture = now.plusHours(24); // 오픈 예정 테스트용 (마지막 회차: 현재시각 + 24시간 후)

            for (int k = 0; k < countToCreate; k++) {
                LocalDate day = startDate.plusDays(k);
                int h = (k % 2 == 0) ? hour : (hour == 19 ? 14 : 19);
                LocalDateTime perfTime = day.atTime(h, minute);

                // 마지막 회차는 오픈 예정 테스트 (현재시간 + 24시간 후 오픈)
                LocalDateTime openTime = (k == countToCreate - 1 && countToCreate > 1) ? openTimeFuture : openTimePast;

                PerformanceSchedule s = new PerformanceSchedule(savedPerformance, perfTime, openTime);
                scheduleRepository.save(s);

                // KOPIS 실제 수용 좌석 수 100% 동일 생성 (64개 단위 섹터, 8x8 배치)
                generateExactKopisSeats(s, actualSeatScale);
            }

            importedList.add(PerformanceResponse.from(savedPerformance));
        }

        return importedList;
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
}
