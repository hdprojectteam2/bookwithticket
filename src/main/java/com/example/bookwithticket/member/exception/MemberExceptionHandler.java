package com.example.bookwithticket.member.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class MemberExceptionHandler {


    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<?> duplicateEmail(
            DuplicateEmailException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message", e.getMessage()
                ));
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<?> loginFailed(
            LoginFailedException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "message", e.getMessage()
                ));
    }
}