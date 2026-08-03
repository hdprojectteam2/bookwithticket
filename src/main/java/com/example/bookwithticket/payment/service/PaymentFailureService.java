package com.example.bookwithticket.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.order.entity.BookOrderEntity;
import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.repository.PaymentRepository;

@Service
public class PaymentFailureService {

    private final PaymentRepository paymentRepository;

    public PaymentFailureService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveFailure(
            BookOrderEntity order,
            String paymentKey,
            String idempotencyKey,
            int amount,
            String failCode,
            String failMessage
    ) {
        PaymentEntity failedPayment =
                PaymentEntity.failed(
                        order,
                        paymentKey,
                        idempotencyKey,
                        amount,
                        failCode,
                        failMessage
                );

        paymentRepository.save(failedPayment);
    }
}