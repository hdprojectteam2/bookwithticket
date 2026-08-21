package com.example.bookwithticket.member.entity;

import com.example.bookwithticket.book.entity.Book;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "recent_book",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_recent_member_book",
        columnNames = {"member_id", "book_id"}
    ),
    indexes = @Index(name = "idx_recent_member_viewed", columnList = "member_id, viewed_at")
)
public class RecentBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    protected RecentBook() {}

    public RecentBook(Member member, Book book) {
        this.member = member;
        this.book = book;
        this.viewedAt = LocalDateTime.now();
    }

    public void touch() { this.viewedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public LocalDateTime getViewedAt() { return viewedAt; }
}
