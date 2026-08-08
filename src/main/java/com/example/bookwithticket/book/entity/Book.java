package com.example.bookwithticket.book.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;


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



    // 카테고리

    private String category;



    // 인기 도서용

    private int viewCount;


    private int salesCount;



    // 등록일

    private LocalDateTime createdAt;



    // 재고

    private int stock;





    public Book(){

    }





    public Book(
            String isbn,
            String title,
            String author,
            String publisher,
            int price,
            String thumbnail,
            String description
    ){

        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.thumbnail = thumbnail;
        this.description = description;

        this.stock = 0;

        this.viewCount = 0;

        this.salesCount = 0;

    }





    @PrePersist
    public void prePersist(){

        this.createdAt =
                LocalDateTime.now();

    }






    public Long getId(){
        return id;
    }


    public String getIsbn(){
        return isbn;
    }


    public String getTitle(){
        return title;
    }


    public String getAuthor(){
        return author;
    }


    public String getPublisher(){
        return publisher;
    }


    public int getPrice(){
        return price;
    }


    public String getThumbnail(){
        return thumbnail;
    }


    public String getDescription(){
        return description;
    }


    public String getCategory(){
        return category;
    }


    public int getViewCount(){
        return viewCount;
    }


    public int getSalesCount(){
        return salesCount;
    }


    public LocalDateTime getCreatedAt(){
        return createdAt;
    }


    public int getStock(){
        return stock;
    }






    public void setTitle(String title){
        this.title = title;
    }


    public void setAuthor(String author){
        this.author = author;
    }


    public void setPublisher(String publisher){
        this.publisher = publisher;
    }


    public void setPrice(int price){
        this.price = price;
    }


    public void setThumbnail(String thumbnail){
        this.thumbnail = thumbnail;
    }


    public void setDescription(String description){
        this.description = description;
    }


    public void setCategory(String category){
        this.category = category;
    }


    public void setStock(int stock){
        this.stock = stock;
    }



    public void increaseViewCount(){

        this.viewCount++;

    }



    public void increaseSalesCount(){

        this.salesCount++;

    }






    public void decreaseStock(int quantity){


        if(quantity <= 0){

            throw new RuntimeException(
                    "수량은 1개 이상이어야 합니다."
            );

        }


        if(stock < quantity){

            throw new RuntimeException(
                    "재고가 부족합니다."
            );

        }


        this.stock -= quantity;

    }





    public void increaseStock(int quantity){


        if(quantity <= 0){

            throw new RuntimeException(
                    "수량은 1개 이상이어야 합니다."
            );

        }


        this.stock += quantity;

    }


}