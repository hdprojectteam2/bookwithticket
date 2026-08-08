package com.example.bookwithticket.payment.dto;

public class PaymentConfirmResponse {

    private final Long paymentId;
    private final String orderNumber;
    private final int amount;
    private final String method;
    private final String status;

    public PaymentConfirmResponse(
            Long paymentId,
            String orderNumber,
            int amount,
            String method,
            String status
    ) {
        this.paymentId = paymentId;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public int getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }
}