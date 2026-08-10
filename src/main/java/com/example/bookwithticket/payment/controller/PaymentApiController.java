package com.example.bookwithticket.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookwithticket.payment.dto.PaymentConfirmRequest;
import com.example.bookwithticket.payment.dto.PaymentConfirmResponse;
import com.example.bookwithticket.payment.dto.PaymentFailureRequest;
import com.example.bookwithticket.payment.service.PaymentService;

@RestController
public class PaymentApiController {

    private final PaymentService paymentService;

    public PaymentApiController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /* 임시 회원 ID */
    private Long getCurrentMemberId() {
        return 1L;
    }

    /* 결제 승인 */
    @PostMapping("/api/payments")
    public ResponseEntity<PaymentConfirmResponse>
            confirmPayment(
                    @RequestBody
                    PaymentConfirmRequest request
            ) {
        Long memberId = getCurrentMemberId();

        PaymentConfirmResponse response = paymentService.confirmPayment(memberId, request);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentErrorResponse>
            handleIllegalArgument(
                    IllegalArgumentException exception
            ) {
        PaymentErrorResponse response = new PaymentErrorResponse("PAYMENT_CONFIRM_FAILED", exception.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
    
    @PostMapping("/api/payments/fail")
    public ResponseEntity<Void> savePaymentFailure(
            @RequestBody PaymentFailureRequest request
    ) {
        Long memberId = getCurrentMemberId();

        paymentService.savePaymentFailure(
                memberId,
                request
        );

        return ResponseEntity.ok().build();
    }
}