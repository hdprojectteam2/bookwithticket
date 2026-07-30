package com.example.bookwithticket.book.dto;

import com.example.bookwithticket.book.entity.Book;

public class BookResponseDto {


    private Long id;

    private String title;

    private String author;

    private String publisher;

    private int price;

    private int stock;

    private String description;



    public BookResponseDto(Book book) {

        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.publisher = book.getPublisher();
        this.price = book.getPrice();
        this.stock = book.getStock();
        this.description = book.getDescription();

    }


    public Long getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }


    public String getAuthor() {
        return author;
    }


    public String getPublisher() {
        return publisher;
    }


    public int getPrice() {
        return price;
    }


    public int getStock() {
        return stock;
    }


    public String getDescription() {
        return description;
    }
}