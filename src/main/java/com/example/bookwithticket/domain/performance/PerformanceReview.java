package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.member.entity.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating;

    private LocalDateTime createdAt;

    public PerformanceReview() {
    }

    public PerformanceReview(Member member, Performance performance, String content, int rating) {
        this.member = member;
        this.performance = performance;
        this.content = content;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public Performance getPerformance() { return performance; }
    public String getContent() { return content; }
    public int getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
