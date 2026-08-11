package com.example.bookwithticket.member.dto;

public class LoginResponseDto {
    private final String tokenType = "Bearer";
    private final String accessToken;

    public LoginResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() { return tokenType; }
    public String getAccessToken() { return accessToken; }
}
