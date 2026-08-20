package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    List<PerformanceReview> findByPerformanceOrderByCreatedAtDesc(Performance performance);

    List<PerformanceReview> findByMember(Member member);

    @Query("select avg(r.rating) from PerformanceReview r where r.performance.id = :performanceId")
    Double findAverageRating(Long performanceId);

    @Query("select count(r) from PerformanceReview r where r.performance.id = :performanceId")
    long countReview(Long performanceId);
}
