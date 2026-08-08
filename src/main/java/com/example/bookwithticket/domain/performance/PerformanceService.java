package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.global.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;

    public PerformanceService(PerformanceRepository performanceRepository, PerformanceScheduleRepository scheduleRepository) {
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
    }
    //4. 넘겨받음 , 검색 실행함  , repository로 이동
    public List<PerformanceResponse> getPerformances(String keyword, PerformanceCategory category) {
        List<Performance> performances;
        
        
        if (keyword != null && !keyword.isBlank()) {
            performances = performanceRepository.findByTitleContainingOrderByIdDesc(keyword.trim());
        } else if (category != null) {
            performances = performanceRepository.findByCategoryOrderByIdDesc(category);
        } else {
            performances = performanceRepository.findAllByOrderByIdDesc();
        }
        
        
        //6. 받아온 내용을 response로 이동, 정리해서 다시 받아오고 리턴 
        List<PerformanceResponse> responseList = new ArrayList<>();
        for (Performance p : performances) {
            PerformanceResponse dto = PerformanceResponse.from(p);
            responseList.add(dto);
        }

        
        return responseList;
    }
    //detail에서 사용 
    public PerformanceResponse getPerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."));
        return PerformanceResponse.from(performance);
    }
    //15. id검색해서 일치하는걸 찾음 
    public List<ScheduleResponse> getSchedules(Long performanceId) {
        
        //16. repository로 이동
        //18. 받아온 제목을 시간순서대로 리스트로 만들어서 반환 
        return scheduleRepository.findByPerformanceIdOrderByPerformanceTimeAsc(performanceId)
                .stream().map(ScheduleResponse::from).toList();
    }
}
