package com.example.bookwithticket.member.dto;

import com.example.bookwithticket.member.entity.FavoriteBook;
import java.time.LocalDateTime;

public class FavoriteBookResponseDto {
    private final Long id;
    private final String title;
    private final String author;
    private final String thumbnail;
    private final int price;
    private final int salePrice;
    private final String category;
    private final LocalDateTime favoritedAt;

    public FavoriteBookResponseDto(FavoriteBook favoriteBook) {
        var book = favoriteBook.getBook();
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.thumbnail = book.getThumbnail();
        this.price = book.getPrice();
        this.salePrice = book.getSalePrice();
        this.category = book.getCategory();
        this.favoritedAt = favoriteBook.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getThumbnail() { return thumbnail; }
    public int getPrice() { return price; }
    public int getSalePrice() { return salePrice; }
    public String getCategory() { return category; }
    public LocalDateTime getFavoritedAt() { return favoritedAt; }
}
