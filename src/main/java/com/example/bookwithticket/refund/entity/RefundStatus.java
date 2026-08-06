package com.example.bookwithticket.refund.entity;

public enum RefundStatus {

	REQUESTED,	// 환불 승인 대기
	APPROVED,	// 환불 승인
	REJECTED,	// 환불 거절
	FAILED,		// 환불 처리 실패
	COMPLETED	// 환불 처리 성공
}
