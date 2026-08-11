package com.example.bookwithticket.member.dto;

public class EmailCheckResponseDto {
    private final boolean available;

    public EmailCheckResponseDto(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() { return available; }
}
