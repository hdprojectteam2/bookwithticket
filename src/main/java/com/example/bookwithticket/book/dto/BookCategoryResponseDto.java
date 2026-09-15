package com.example.bookwithticket.book.dto;

import com.example.bookwithticket.book.entity.BookCategory;

public class BookCategoryResponseDto {

    private final String code;
    private final String name;

    public BookCategoryResponseDto(BookCategory category) {
        this.code = category.name();
        this.name = category.getLabel();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
