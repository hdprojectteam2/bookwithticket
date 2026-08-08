package com.example.bookwithticket.refund.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookwithticket.refund.entity.RefundEntity;
import com.example.bookwithticket.refund.entity.RefundStatus;

public interface RefundRepository extends JpaRepository<RefundEntity, Long> {
	
	boolean existsByPaymentId(Long paymentId);
	
	Optional<RefundEntity> findByIdAndStatus(Long refundId, RefundStatus status);
	
    Optional<RefundEntity> findByPaymentId(Long paymentId);
}
