package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.global.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.domain.reservation.Seat;
import com.example.bookwithticket.domain.reservation.SeatRepository;

@Service
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final com.example.bookwithticket.domain.reservation.ReservationRepository reservationRepository;
    private final com.example.bookwithticket.cart.repository.PerformanceCartItemRepository performanceCartItemRepository;

    public PerformanceService(
            PerformanceRepository performanceRepository,
            PerformanceScheduleRepository scheduleRepository,
            SeatRepository seatRepository,
            com.example.bookwithticket.domain.reservation.ReservationRepository reservationRepository,
            com.example.bookwithticket.cart.repository.PerformanceCartItemRepository performanceCartItemRepository) {
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.performanceCartItemRepository = performanceCartItemRepository;
    }
    //4. 넘겨받음 , 검색 실행함  , repository로 이동
    public List<PerformanceResponse> getPerformances(String keyword, PerformanceCategory category, boolean includeInactive) {
        List<Performance> performances;
        
        if (keyword != null && !keyword.isBlank()) {
            performances = includeInactive
                    ? performanceRepository.findByTitleContainingOrderByIdDesc(keyword.trim())
                    : performanceRepository.findByTitleContainingAndActiveTrueOrderByIdDesc(keyword.trim());
        } else if (category != null) {
            performances = includeInactive
                    ? performanceRepository.findByCategoryOrderByIdDesc(category)
                    : performanceRepository.findByCategoryAndActiveTrueOrderByIdDesc(category);
        } else {
            performances = includeInactive
                    ? performanceRepository.findAllByOrderByIdDesc()
                    : performanceRepository.findByActiveTrueOrderByIdDesc();
        }
        
        return performances.stream()
                .map(PerformanceResponse::from)
                .toList();
    }

    //detail에서 사용 
    public PerformanceResponse getPerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));
        if (!performance.isActive()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "삭제되거나 비활성화된 공연입니다.");
        }
        return PerformanceResponse.from(performance);
    }

    public List<ScheduleResponse> getSchedules(Long performanceId) {
        return scheduleRepository.findByPerformanceIdOrderByPerformanceTimeAsc(performanceId)
                .stream().map(s -> {
                    List<Seat> seats = seatRepository.findByScheduleIdOrderByIdAsc(s.getId());
                    int total = seats.size();
                    int available = (int) seats.stream().filter(seat -> seat.getStatus() == com.example.bookwithticket.domain.reservation.SeatStatus.AVAILABLE).count();
                    return ScheduleResponse.from(s, total, available);
                }).toList();
    }

    // 관리자: 공연 등록
    @Transactional
    public PerformanceResponse createPerformance(PerformanceRequestDto dto) {
        Performance performance = new Performance(
                dto.title(),
                dto.category(),
                dto.venue(),
                dto.posterUrl(),
                dto.runtimeMinutes(),
                dto.description(),
                dto.originalBookId()
        );
        return PerformanceResponse.from(performanceRepository.save(performance));
    }

    // 관리자: 공연 수정
    @Transactional
    public PerformanceResponse updatePerformance(Long id, PerformanceRequestDto dto) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));
        performance.update(
                dto.title(),
                dto.category(),
                dto.venue(),
                dto.posterUrl(),
                dto.runtimeMinutes(),
                dto.description(),
                dto.originalBookId()
        );
        return PerformanceResponse.from(performanceRepository.save(performance));
    }

    // 관리자: 공연 비활성화 (Soft Delete) 및 장바구니 연쇄 자동 삭제
    @Transactional
    public void deletePerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));
        performance.deactivate();

        List<PerformanceSchedule> schedules = scheduleRepository.findByPerformanceIdOrderByPerformanceTimeAsc(id);
        for (PerformanceSchedule s : schedules) {
            performanceCartItemRepository.deleteByPerformanceScheduleId(s.getId());
        }
    }

    // 관리자: 공연 재활성화 (복구)
    @Transactional
    public void activatePerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));
        performance.activate();
    }

    // 관리자: 공연 영구 완전 삭제 (Hard Delete)
    @Transactional
    public void hardDeletePerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));
        List<PerformanceSchedule> schedules = scheduleRepository.findByPerformanceIdOrderByPerformanceTimeAsc(id);
        for (PerformanceSchedule s : schedules) {
            performanceCartItemRepository.deleteByPerformanceScheduleId(s.getId());
            reservationRepository.deleteByScheduleId(s.getId());
            seatRepository.deleteByScheduleId(s.getId());
            scheduleRepository.delete(s);
        }
        performanceRepository.delete(performance);
    }

    // 관리자: 회차 및 좌석 자동 배치 생성
    @Transactional
    public ScheduleResponse createScheduleAndSeats(Long performanceId, ScheduleCreateRequest dto) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));

        PerformanceSchedule schedule = new PerformanceSchedule(
                performance,
                dto.performanceTime(),
                dto.ticketOpenTime()
        );
        PerformanceSchedule savedSchedule = scheduleRepository.save(schedule);

        int totalSeats = (dto.totalSeats() > 0) ? dto.totalSeats() : (performance.getSeatscale() > 0 ? performance.getSeatscale() : 100);
        int vipCount = Math.max(1, (int) Math.round(totalSeats * 0.10));
        int rCount = Math.max(1, (int) Math.round(totalSeats * 0.10));
        int sCount = Math.max(1, (int) Math.round(totalSeats * 0.40));

        List<Seat> seatList = new ArrayList<>();
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
            seatList.add(new Seat(savedSchedule, seatNumber, price));
        }
        seatRepository.saveAll(seatList);

        return ScheduleResponse.from(savedSchedule, totalSeats, totalSeats);
    }
}
