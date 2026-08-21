package com.example.bookwithticket.refund.service;

import com.example.bookwithticket.refund.dto.RefundResponse;

public interface RefundService {
	RefundResponse requestBookRefund(Long memberId, String orderNumber, String reason, String returnMethod);

	RefundResponse approveBookRefund(Long adminId, Long refundId);

	RefundResponse rejectBookRefund(Long adminId, Long refundId);

	RefundResponse requestPerformanceRefund(Long memberId, String reservationNumber, String reason);

	RefundResponse forceBookRefund(Long adminId, String orderNumber);

	RefundResponse forcePerformanceRefund(Long adminId, Long reservationId);
}
