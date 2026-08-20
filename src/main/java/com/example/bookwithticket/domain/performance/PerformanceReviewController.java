package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/performances")
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;
    private final MemberService memberService;

    public PerformanceReviewController(PerformanceReviewService performanceReviewService,
                                        MemberService memberService) {
        this.performanceReviewService = performanceReviewService;
        this.memberService = memberService;
    }

    // 리뷰 작성
    @PostMapping("/{performanceId}/reviews")
    public PerformanceReviewResponseDto save(
            @PathVariable Long performanceId,
            @RequestBody PerformanceReviewRequestDto requestDto,
            Authentication authentication) {

        if (authentication == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Member member = memberService.findMyInfo(authentication.getName());
        PerformanceReview review = performanceReviewService.save(performanceId, member, requestDto);
        return new PerformanceReviewResponseDto(review);
    }

    // 리뷰 조회
    @GetMapping("/{performanceId}/reviews")
    public List<PerformanceReviewResponseDto> findAll(@PathVariable Long performanceId) {
        return performanceReviewService.findAll(performanceId)
                .stream()
                .map(PerformanceReviewResponseDto::new)
                .toList();
    }

    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public String delete(@PathVariable Long reviewId, Authentication authentication) {
        Member member = memberService.findMyInfo(authentication.getName());
        performanceReviewService.delete(reviewId, member);
        return "리뷰 삭제 완료";
    }

    // 평균 평점 + 리뷰 수
    @GetMapping("/{performanceId}/reviews/info")
    public Map<String, Object> reviewInfo(@PathVariable Long performanceId) {
        Double averageRating = performanceReviewService.getAverageRating(performanceId);
        long reviewCount = performanceReviewService.getReviewCount(performanceId);
        return Map.of("averageRating", averageRating, "reviewCount", reviewCount);
    }
}
