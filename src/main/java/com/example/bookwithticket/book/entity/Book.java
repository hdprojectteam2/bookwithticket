package com.example.bookwithticket.book.entity;


import jakarta.persistence.*;


@Entity
@Table(name = "book")
public class Book {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String isbn;


    @Column(nullable = false)
    private String title;


    private String author;


    private String publisher;


    private int price;


    private String thumbnail;


    @Column(columnDefinition = "TEXT")
    private String description;


    // 재고
    private int stock;



    public Book() {
    }



    public Book(
            String isbn,
            String title,
            String author,
            String publisher,
            int price,
            String thumbnail,
            String description
    ) {

        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.thumbnail = thumbnail;
        this.description = description;

        this.stock = 0;
    }



    public Long getId() {
        return id;
    }


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


    public int getStock() {
        return stock;
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


    public void setStock(int stock) {
        this.stock = stock;
    }



    // 주문 시 재고 감소
    public void decreaseStock(int quantity) {


        if(quantity <= 0) {
            throw new RuntimeException("수량은 1개 이상이어야 합니다.");
        }


        if(stock < quantity) {
            throw new RuntimeException("재고가 부족합니다.");
        }


        this.stock -= quantity;

    }



    // 취소/입고 시 재고 증가
    public void increaseStock(int quantity) {


        if(quantity <= 0) {
            throw new RuntimeException("수량은 1개 이상이어야 합니다.");
        }


        this.stock += quantity;

    }

}