package com.example.bookwithticket.book.dto;

import com.example.bookwithticket.book.entity.Book;
import lombok.Getter;

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
    private int stock;


    public BookResponseDto(Book book) {

        this.id = book.getId();
        this.isbn = book.getIsbn();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.publisher = book.getPublisher();
        this.price = book.getPrice();
        this.thumbnail = book.getThumbnail();
        this.description = book.getDescription();
        this.stock = book.getStock();

    }


    // 테스트용으로 getter 직접 추가
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
}