package com.example.bookwithticket.book.dto;

public class BookRequestDto {


    private String title;

    private String author;

    private String publisher;

    private int price;

    private int stock;

    private String description;



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