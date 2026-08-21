package com.example.bookwithticket.refund.dto;

public class RefundRequest {
	private String reason;
	private String returnMethod;

	public String getReason() {
		return reason;
	}

	public String getReturnMethod() {
		return returnMethod;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setReturnMethod(String returnMethod) {
		this.returnMethod = returnMethod;
	}
}
