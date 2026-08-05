package com.example.bookwithticket.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.entity.PaymentStatus;

public interface PaymentRepository
        extends JpaRepository<PaymentEntity, Long> {

	// 중복 결제 방지
    boolean existsByPaymentKey(String paymentKey);

    boolean existsByBookOrderIdAndStatus(Long bookOrderId, PaymentStatus status);

    boolean existsByReservationIdAndStatus(Long reservationId, PaymentStatus status);
}