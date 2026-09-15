package com.example.bookwithticket.member.exception;

public class FavoriteNotFoundException extends RuntimeException {
    public FavoriteNotFoundException() { super("관심 도서에서 찾을 수 없습니다."); }
}
