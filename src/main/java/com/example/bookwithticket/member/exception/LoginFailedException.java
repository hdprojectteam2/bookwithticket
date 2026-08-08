package com.example.bookwithticket.member.exception;

public class LoginFailedException extends RuntimeException {

    public LoginFailedException(String message) {
        super(message);
    }
}