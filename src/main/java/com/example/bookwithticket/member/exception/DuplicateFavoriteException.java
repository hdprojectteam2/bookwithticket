package com.example.bookwithticket.member.exception;

public class DuplicateFavoriteException extends RuntimeException {
    public DuplicateFavoriteException() { super("이미 관심 도서에 추가된 책입니다."); }
}
