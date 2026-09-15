package com.example.bookwithticket.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class BookRequestDto {

    @NotBlank(message = "ISBN은 필수입니다.")
    @Size(max = 20, message = "ISBN은 20자 이하로 입력해주세요.")
    private String isbn;

    @NotBlank(message = "도서명은 필수입니다.")
    @Size(max = 300, message = "도서명은 300자 이하로 입력해주세요.")
    private String title;

    private String author;
    private String publisher;

    @Min(value = 0, message = "정가는 0원 이상이어야 합니다.")
    private int price;

    @Min(value = 0, message = "판매가는 0원 이상이어야 합니다.")
    private int salePrice;

    private String thumbnail;
    private String description;
    private String category;
    private String originalCategoryName;
    private LocalDate publishedDate;

    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
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

    public int getSalePrice() {
        return salePrice;
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

    public String getOriginalCategoryName() {
        return originalCategoryName;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
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

    public void setSalePrice(int salePrice) {
        this.salePrice = salePrice;
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

    public void setOriginalCategoryName(String originalCategoryName) {
        this.originalCategoryName = originalCategoryName;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
