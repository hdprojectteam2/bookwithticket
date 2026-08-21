package com.example.bookwithticket.payment.dto;

public class PaymentCheckoutResponse {

    private final String orderNumber;
    private final String orderName;
    private final int totalPrice;
    private final String clientKey;
    private final String seatNumber;

    public PaymentCheckoutResponse(
            String orderNumber,
            String orderName,
            int totalPrice,
            String clientKey,
            String seatNumber
    ) {
        this.orderNumber = orderNumber;
        this.orderName = orderName;
        this.totalPrice = totalPrice;
        this.clientKey = clientKey;
        this.seatNumber = seatNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getOrderName() {
        return orderName;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getSeatNumber() {
        return seatNumber;
    }
}