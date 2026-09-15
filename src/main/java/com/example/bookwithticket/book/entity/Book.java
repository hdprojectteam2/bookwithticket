package com.example.bookwithticket.book.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "book",
        indexes = {
                @Index(name = "idx_book_title", columnList = "title"),
                @Index(name = "idx_book_category", columnList = "category"),
                @Index(name = "idx_book_published_date", columnList = "publishedDate")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_book_isbn", columnNames = "isbn")
)
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false, length = 300)
    private String title;

    private String author;
    private String publisher;
    private int price;
    private int salePrice;

    @Column(length = 1000)
    private String thumbnail;

    @Column(columnDefinition = "TEXT")
    private String description;

    // dev/팀 코드 호환을 위해 String 유지
    private String category;

    private String originalCategoryName;
    private LocalDate publishedDate;
    private int viewCount;
    private int salesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int stock;

    public Book() {}

    // 기존 dev 생성자 유지
    public Book(String isbn, String title, String author, String publisher,
                int price, String thumbnail, String description) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.salePrice = price;
        this.thumbnail = thumbnail;
        this.description = description;
        this.category = "기타";
        this.stock = 0;
    }

    public static Book create(
            String isbn, String title, String author, String publisher,
            int price, int salePrice, String thumbnail, String description,
            String category, String originalCategoryName,
            LocalDate publishedDate, int stock
    ) {
        Book book = new Book(isbn, title, author, publisher, price, thumbnail, description);
        book.salePrice = salePrice > 0 ? salePrice : price;
        book.category = (category == null || category.isBlank()) ? "기타" : category;
        book.originalCategoryName = originalCategoryName;
        book.publishedDate = publishedDate;
        book.stock = Math.max(stock, 0);
        return book;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.salePrice <= 0) this.salePrice = this.price;
        if (this.category == null || this.category.isBlank()) this.category = "기타";
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public void update(String title, String author, String publisher,
                       int price, int salePrice, String thumbnail, String description,
                       String category, String originalCategoryName,
                       LocalDate publishedDate, int stock) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.salePrice = salePrice > 0 ? salePrice : price;
        this.thumbnail = thumbnail;
        this.description = description;
        this.category = (category == null || category.isBlank()) ? "기타" : category;
        this.originalCategoryName = originalCategoryName;
        this.publishedDate = publishedDate;
        this.stock = Math.max(stock, 0);
    }

    public void increaseViewCount() { this.viewCount++; }
    public void increaseSalesCount() { this.salesCount++; }
    public void increaseSalesCount(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("판매 수량은 1개 이상이어야 합니다.");
        this.salesCount += quantity;
    }
    public void decreaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        if (stock < quantity) throw new IllegalStateException("재고가 부족합니다.");
        this.stock -= quantity;
    }
    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        this.stock += quantity;
    }
    public void changeStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        this.stock = stock;
    }

    // 기존 getter/setter 유지
    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public int getPrice() { return price; }
    public int getSalePrice() { return salePrice > 0 ? salePrice : price; }
    public String getThumbnail() { return thumbnail; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BookCategory getCategoryType() { return BookCategory.from(category); }
    public String getOriginalCategoryName() { return originalCategoryName; }
    public LocalDate getPublishedDate() { return publishedDate; }
    public int getViewCount() { return viewCount; }
    public int getSalesCount() { return salesCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public int getStock() { return stock; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setPrice(int price) { this.price = price; if (salePrice <= 0) salePrice = price; }
    public void setSalePrice(int salePrice) { this.salePrice = salePrice; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setOriginalCategoryName(String originalCategoryName) { this.originalCategoryName = originalCategoryName; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }
    public void setStock(int stock) { this.stock = Math.max(stock, 0); }
}
