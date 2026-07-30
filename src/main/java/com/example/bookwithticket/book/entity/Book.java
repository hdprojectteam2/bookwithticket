package com.example.bookwithticket.book.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String title;


    @Column(nullable = false)
    private String author;


    private String publisher;


    private int price;


    private int stock;


    private String description;


    public Book(
            String title,
            String author,
            String publisher,
            int price,
            int stock,
            String description
    ) {

        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.stock = stock;
        this.description = description;
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