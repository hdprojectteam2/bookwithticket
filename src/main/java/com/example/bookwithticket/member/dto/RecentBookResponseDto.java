package com.example.bookwithticket.member.dto;

import com.example.bookwithticket.member.entity.RecentBook;
import java.time.LocalDateTime;

public class RecentBookResponseDto {
    private final Long id;
    private final String title;
    private final String author;
    private final String thumbnail;
    private final int salePrice;
    private final LocalDateTime viewedAt;

    public RecentBookResponseDto(RecentBook recentBook) {
        var book = recentBook.getBook();
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.thumbnail = book.getThumbnail();
        this.salePrice = book.getSalePrice();
        this.viewedAt = recentBook.getViewedAt();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getThumbnail() { return thumbnail; }
    public int getSalePrice() { return salePrice; }
    public LocalDateTime getViewedAt() { return viewedAt; }
}
