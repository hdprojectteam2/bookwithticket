package com.example.bookwithticket.book.dto;


public class BookImportRequestDto {


    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private int price;

    private String thumbnail;

    private String description;



    public String getIsbn() {
        return isbn;
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


    public String getThumbnail() {
        return thumbnail;
    }


    public String getDescription() {
        return description;
    }
}