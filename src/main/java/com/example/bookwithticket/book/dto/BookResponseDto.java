package com.example.bookwithticket.book.dto;


import com.example.bookwithticket.book.entity.Book;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class BookResponseDto {


    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private int price;

    private String thumbnail;

    private String description;


    private String category;


    private int stock;


    private int viewCount;


    private int salesCount;


    private LocalDateTime createdAt;





    public BookResponseDto(Book book){


        this.id = book.getId();

        this.isbn = book.getIsbn();

        this.title = book.getTitle();

        this.author = book.getAuthor();

        this.publisher = book.getPublisher();

        this.price = book.getPrice();

        this.thumbnail = book.getThumbnail();

        this.description = book.getDescription();


        this.category = book.getCategory();


        this.stock = book.getStock();


        this.viewCount = book.getViewCount();


        this.salesCount = book.getSalesCount();


        this.createdAt = book.getCreatedAt();


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


    public int getStock(){
        return stock;
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

}