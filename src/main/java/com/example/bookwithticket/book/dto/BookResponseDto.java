package com.example.bookwithticket.book.dto;

import com.example.bookwithticket.book.entity.Book;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookResponseDto {

    private final Long id;
    private final String isbn;
    private final String title;
    private final String author;
    private final String publisher;
    private final int price;
    private final int salePrice;
    private final int discountRate;
    private final String thumbnail;
    private final String description;
    private final String category;
    private final String categoryCode;
    private final String originalCategoryName;
    private final LocalDate publishedDate;
    private final int stock;
    private final int viewCount;
    private final int salesCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BookResponseDto(Book book) {
        this.id = book.getId();
        this.isbn = book.getIsbn();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.publisher = book.getPublisher();
        this.price = book.getPrice();
        this.salePrice = book.getSalePrice();
        this.discountRate = calculateDiscountRate(book.getPrice(), book.getSalePrice());
        this.thumbnail = book.getThumbnail();
        this.description = book.getDescription();
        this.category = book.getCategory();
        this.categoryCode = book.getCategoryType() != null
                ? book.getCategoryType().name()
                : "ETC";
        this.originalCategoryName = book.getOriginalCategoryName();
        this.publishedDate = book.getPublishedDate();
        this.stock = book.getStock();
        this.viewCount = book.getViewCount();
        this.salesCount = book.getSalesCount();
        this.createdAt = book.getCreatedAt();
        this.updatedAt = book.getUpdatedAt();
    }

    private int calculateDiscountRate(int price, int salePrice) {
        if (price <= 0 || salePrice >= price) {
            return 0;
        }
        return (int) Math.round((price - salePrice) * 100.0 / price);
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public int getPrice() { return price; }
    public int getSalePrice() { return salePrice; }
    public int getDiscountRate() { return discountRate; }
    public String getThumbnail() { return thumbnail; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getCategoryCode() { return categoryCode; }
    public String getOriginalCategoryName() { return originalCategoryName; }
    public LocalDate getPublishedDate() { return publishedDate; }
    public int getStock() { return stock; }
    public int getViewCount() { return viewCount; }
    public int getSalesCount() { return salesCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
