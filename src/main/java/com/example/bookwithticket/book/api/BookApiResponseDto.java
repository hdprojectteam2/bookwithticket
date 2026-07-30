package com.example.bookwithticket.book.api;


public class BookApiResponseDto {


    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private int price;

    private String imageUrl;



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


    public String getImageUrl() {
        return imageUrl;
    }
}