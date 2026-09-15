package com.example.bookwithticket.payment.service;

import com.example.bookwithticket.payment.dto.PaymentConfirmRequest;
import com.example.bookwithticket.payment.dto.PaymentConfirmResponse;
import com.example.bookwithticket.payment.dto.PaymentFailureRequest;

public interface PaymentService {

    PaymentConfirmResponse confirmPayment(Long memberId, PaymentConfirmRequest request);

    void savePaymentFailure(Long memberId, PaymentFailureRequest request);
}