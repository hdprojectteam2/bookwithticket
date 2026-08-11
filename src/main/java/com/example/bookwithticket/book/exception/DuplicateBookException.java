package com.example.bookwithticket.book.exception;

public class DuplicateBookException extends RuntimeException {

    public DuplicateBookException(String isbn) {
        super("이미 등록된 ISBN입니다. isbn=" + isbn);
    }
}
