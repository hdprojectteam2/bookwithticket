package com.example.bookwithticket.payment.entity;

public enum PaymentMethod {

	ACCOUNT_TRANSFER,	// 계좌 이체
    CARD,				// 카드 결제
    TOSS_PAY,			// 토스 페이
    PAYCO,				// 페이코
    KAKAO_PAY,			// 카카오 페이
    NAVER_PAY,			// 네이버 페이
    UNKNOWN				// 이외
}