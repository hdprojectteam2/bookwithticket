package com.example.bookwithticket.member.entity;

import com.example.bookwithticket.book.entity.Book;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "favorite_book",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_favorite_member_book",
        columnNames = {"member_id", "book_id"}
    ),
    indexes = @Index(name = "idx_favorite_member_created", columnList = "member_id, created_at")
)
public class FavoriteBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected FavoriteBook() {}

    public FavoriteBook(Member member, Book book) {
        this.member = member;
        this.book = book;
    }

    @PrePersist
    void prePersist() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
