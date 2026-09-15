package com.example.bookwithticket.order.entity;

public enum OrderStatus {

    PAYMENT_PENDING,	// 결제 대기
    PAID,				// 결제 완료
    CANCELLED,			// 주문 취소
    REFUNDED			// 환불 완료
}