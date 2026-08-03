package com.example.bookwithticket.payment.dto;

public class PaymentCheckoutResponse {

    private final String orderNumber;
    private final String orderName;
    private final int amount;

    public PaymentCheckoutResponse(String orderNumber, String orderName, int amount) {
        this.orderNumber = orderNumber;
        this.orderName = orderName;
        this.amount = amount;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getOrderName() {
        return orderName;
    }

    public int getAmount() {
        return amount;
    }
}