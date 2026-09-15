package com.example.bookwithticket.refund.dto;

public class RefundResponse {
	private final Long refundId;
	private final String status;
	private final String message;
	
	public RefundResponse(Long refundId, String status, String message) {
		this.refundId = refundId;
		this.status = status;
		this.message = message;
	}

	public Long getRefundId() {
		return refundId;
	}

	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
	
}
