package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.global.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }
    //2. 검색시 도착 
    @GetMapping
    public ApiResponse<List<PerformanceResponse>> getPerformances(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category) {
        PerformanceCategory categoryEnum = null;
        
        //3. 키워드 챙겨서 service로 넘김 
        //7. 리턴 받은 데이터를 index로 리턴함.
        return ApiResponse.ok(performanceService.getPerformances(keyword, categoryEnum));
    }
    //상세보기 
    @GetMapping("/{id}")
    public ApiResponse<PerformanceResponse> getPerformance(@PathVariable("id") Long id) {
        return ApiResponse.ok(performanceService.getPerformance(id));
    }
    //회차조회, 14.  service로 이동 
    @GetMapping("/{id}/schedules")
    public ApiResponse<List<ScheduleResponse>> getSchedules(@PathVariable("id") Long id) {
    	//19. 리스트를 반환함 
        return ApiResponse.ok(performanceService.getSchedules(id));
    }
}
