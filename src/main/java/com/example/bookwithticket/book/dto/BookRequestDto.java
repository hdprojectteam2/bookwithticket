package com.example.bookwithticket.book.dto;


public class BookRequestDto {


    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private int price;

    private String thumbnail;

    private String description;


    // 추가
    private String category;


    private int stock;





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


    public String getCategory() {
        return category;
    }


    public int getStock() {
        return stock;
    }





    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public void setAuthor(String author) {
        this.author = author;
    }


    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }


    public void setPrice(int price) {
        this.price = price;
    }


    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public void setCategory(String category) {
        this.category = category;
    }


    public void setStock(int stock) {
        this.stock = stock;
    }

}