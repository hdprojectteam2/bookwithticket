package com.example.bookwithticket.payment.controller;

public class PaymentErrorResponse {

    private final String code;
    private final String message;

    public PaymentErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}