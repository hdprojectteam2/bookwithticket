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

    public PerformanceService(
            PerformanceRepository performanceRepository,
            PerformanceScheduleRepository scheduleRepository,
            SeatRepository seatRepository) {
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
    }
    //4. 넘겨받음 , 검색 실행함  , repository로 이동
    public List<PerformanceResponse> getPerformances(String keyword, PerformanceCategory category, boolean includeInactive) {
        List<Performance> performances;
        
        if (keyword != null && !keyword.isBlank()) {
            performances = performanceRepository.findByTitleContainingOrderByIdDesc(keyword.trim());
        } else if (category != null) {
            performances = performanceRepository.findByCategoryOrderByIdDesc(category);
        } else {
            performances = performanceRepository.findAllByOrderByIdDesc();
        }
        
        List<PerformanceResponse> responseList = new ArrayList<>();
        for (Performance p : performances) {
            if (includeInactive || p.isActive()) {
                responseList.add(PerformanceResponse.from(p));
            }
        }
        return responseList;
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
                .stream().map(ScheduleResponse::from).toList();
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

    // 관리자: 공연 비활성화 (Soft Delete)
    @Transactional
    public void deletePerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));
        performance.deactivate();
    }

    // 관리자: 공연 재활성화 (복구)
    @Transactional
    public void activatePerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));
        performance.activate();
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

        int totalSeats = dto.totalSeats() > 0 ? dto.totalSeats() : 10;
        int price = dto.seatPrice() > 0 ? dto.seatPrice() : 150000;

        for (int i = 1; i <= totalSeats; i++) {
            char row = (char) ('A' + (i - 1) / 10);
            int num = ((i - 1) % 10) + 1;
            String seatNumber = row + "-" + num;
            seatRepository.save(new Seat(savedSchedule, seatNumber, price));
        }

        return ScheduleResponse.from(savedSchedule);
    }
}
