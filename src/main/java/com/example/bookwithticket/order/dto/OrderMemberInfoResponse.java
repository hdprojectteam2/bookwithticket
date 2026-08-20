package com.example.bookwithticket.order.dto;

public record OrderMemberInfoResponse(String name, String phone, String zipcode, String address, String detailAddress) {
}