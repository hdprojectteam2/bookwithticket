package com.example.bookwithticket.order.entity;

import java.time.LocalDateTime;

import com.example.bookwithticket.book.entity.Book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "book_order_item")
public class BookOrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_order_id", nullable = false)
    private BookOrderEntity bookOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(
            name = "book_title_snapshot",
            nullable = false,
            length = 255
    )
    private String bookTitleSnapshot;

    @Column(name = "price_snapshot", nullable = false)
    private int priceSnapshot;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected BookOrderItemEntity() {
    }

    public BookOrderItemEntity(
            BookOrderEntity bookOrder,
            Book book,
            int quantity
    ) {
        this.bookOrder = bookOrder;
        this.book = book;
        this.bookTitleSnapshot = book.getTitle();
        this.priceSnapshot = book.getSalePrice();
        this.quantity = quantity;
    }

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BookOrderEntity getBookOrder() {
        return bookOrder;
    }

    public Book getBook() {
        return book;
    }

    public String getBookTitleSnapshot() {
        return bookTitleSnapshot;
    }

    public int getPriceSnapshot() {
        return priceSnapshot;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getTotalPrice() {
        return priceSnapshot * quantity;
    }


}