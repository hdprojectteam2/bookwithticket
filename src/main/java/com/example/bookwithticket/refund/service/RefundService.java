package com.example.bookwithticket.refund.service;

import com.example.bookwithticket.refund.dto.RefundResponse;

public interface RefundService {
	RefundResponse requestBookRefund(Long memberId, String orderNumber, String reason);
	
	RefundResponse approveBookRefund(Long adminId, Long refundId);
	
	RefundResponse rejectBookRefund(Long adminId, Long refundId);
}
