package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.member.entity.Member;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerformanceReviewService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final PerformanceRepository performanceRepository;

    public PerformanceReviewService(PerformanceReviewRepository performanceReviewRepository,
                                    PerformanceRepository performanceRepository) {
        this.performanceReviewRepository = performanceReviewRepository;
        this.performanceRepository = performanceRepository;
    }

    // 리뷰 작성
    public PerformanceReview save(Long performanceId, Member member, PerformanceReviewRequestDto requestDto) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다."));

        if (requestDto.getRating() < 1 || requestDto.getRating() > 5) {
            throw new RuntimeException("평점은 1점부터 5점까지 가능합니다.");
        }

        PerformanceReview review = new PerformanceReview(
                member, performance, requestDto.getContent(), requestDto.getRating()
        );

        return performanceReviewRepository.save(review);
    }

    // 특정 공연 리뷰 조회
    public List<PerformanceReview> findAll(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다."));

        return performanceReviewRepository.findByPerformanceOrderByCreatedAtDesc(performance);
    }

    // 리뷰 삭제
    public void delete(Long reviewId, Member member) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        if (!review.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        performanceReviewRepository.delete(review);
    }

    // 평균 평점
    public Double getAverageRating(Long performanceId) {
        Double avg = performanceReviewRepository.findAverageRating(performanceId);
        if (avg == null) return 0.0;
        return Math.round(avg * 10) / 10.0;
    }

    // 리뷰 개수
    public long getReviewCount(Long performanceId) {
        return performanceReviewRepository.countReview(performanceId);
    }

    // 회원별 리뷰 조회
    public List<PerformanceReview> findByMember(Member member) {
        return performanceReviewRepository.findByMember(member);
    }
}
