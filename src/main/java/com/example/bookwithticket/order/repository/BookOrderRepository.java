package com.example.bookwithticket.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookwithticket.order.entity.BookOrderEntity;
import com.example.bookwithticket.order.entity.OrderStatus;

public interface BookOrderRepository
        extends JpaRepository<BookOrderEntity, Long> {

    boolean existsByOrderNumber(String orderNumber);

    /*회원의 PAYMENT_PENDING 상태 주문 조회 */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.book"})
    Optional<BookOrderEntity>
    findFirstByMemberIdAndOrderStatusOrderByCreatedAtDesc(
    		Long memberId,
    		OrderStatus orderStatus
    		);


    @EntityGraph(attributePaths = {"orderItems", "orderItems.book"})
    Optional<BookOrderEntity>
    findByOrderNumberAndMemberIdAndOrderStatus(
            String orderNumber,
            Long memberId,
            OrderStatus orderStatus
    );
    
    /* 주문번호와 회원 번호 검증 */
    Optional<BookOrderEntity> findByOrderNumberAndMemberId(
            String orderNumber,
            Long memberId
    );
    
}