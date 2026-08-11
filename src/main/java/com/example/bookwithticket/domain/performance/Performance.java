package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "performances")
public class Performance extends BaseTimeEntity {
	//1씩 자동 증가
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerformanceCategory category;

    @Column(nullable = false, length = 100)
    private String venue;

    private String posterUrl;

    @Column(nullable = false)
    private int runtimeMinutes;

    @Column(length = 2000)
    private String description;

    private Long originalBookId;

    @Column(nullable = false)
    private boolean active = true;

    protected Performance() {}

    public Performance(String title, PerformanceCategory category, String venue, String posterUrl, int runtimeMinutes, String description) {
        this(title, category, venue, posterUrl, runtimeMinutes, description, null);
    }

    public Performance(String title, PerformanceCategory category, String venue, String posterUrl, int runtimeMinutes, String description, Long originalBookId) {
        this.title = title;
        this.category = category;
        this.venue = venue;
        this.posterUrl = posterUrl;
        this.runtimeMinutes = runtimeMinutes;
        this.description = description;
        this.originalBookId = originalBookId;
        
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public PerformanceCategory getCategory() { return category; }
    public String getVenue() { return venue; }
    public String getPosterUrl() { return posterUrl; }
    public int getRuntimeMinutes() { return runtimeMinutes; }
    public String getDescription() { return description; }
    public Long getOriginalBookId() { return originalBookId; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }
   

    
    
    public void update(String title, PerformanceCategory category, String venue, String posterUrl, int runtimeMinutes, String description, Long originalBookId) {
        if (title != null && !title.isBlank()) this.title = title;
        if (category != null) this.category = category;
        if (venue != null && !venue.isBlank()) this.venue = venue;
        if (posterUrl != null) this.posterUrl = posterUrl;
        if (runtimeMinutes > 0) this.runtimeMinutes = runtimeMinutes;
        if (description != null) this.description = description;
        if (originalBookId != null) this.originalBookId = originalBookId;
    }

   
    
}
