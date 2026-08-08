package com.example.bookwithticket.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookwithticket.cart.entity.PerformanceCartItemEntity;

public interface PerformanceCartItemRepository
        extends JpaRepository<PerformanceCartItemEntity, Long> {

    boolean existsByCartIdAndPerformanceScheduleId(
            Long cartId,
            Long performanceScheduleId
    );

    @EntityGraph(
        attributePaths = {
            "performanceSchedule",
            "performanceSchedule.performance"
        }
    )
    List<PerformanceCartItemEntity>
    findAllByCartMemberIdOrderByCreatedAtDesc(
            Long memberId
    );

    Optional<PerformanceCartItemEntity>
    findByIdAndCartMemberId(Long cartItemId, Long memberId);
    
    void deleteByCartMemberId(Long memberId);
}