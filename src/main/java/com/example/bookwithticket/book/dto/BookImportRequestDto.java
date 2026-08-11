package com.example.bookwithticket.book.dto;

public class BookImportRequestDto {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int price;
    private String thumbnail;
    private String description;

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
