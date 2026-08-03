package com.example.bookwithticket.payment.dto;

public class PaymentConfirmRequest {

    private String paymentKey;
    private String orderId;
    private int amount;

    public PaymentConfirmRequest() {
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}