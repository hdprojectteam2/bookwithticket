package com.example.bookwithticket.payment.entity;

public enum PaymentStatus {

    DONE,       // 결제 완료
    CANCELED,   // 결제 취소
    FAILED,     // 결제 실패
    EXPIRED		// 결제 시간 만료
}