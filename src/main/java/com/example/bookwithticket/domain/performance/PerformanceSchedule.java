package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_schedules")
public class PerformanceSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // n+1 방지 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //외래키 
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @Column(nullable = false)
    private LocalDateTime performanceTime;

    @Column(nullable = false)
    private LocalDateTime ticketOpenTime;

    protected PerformanceSchedule() {}

    public PerformanceSchedule(Performance performance, LocalDateTime performanceTime, LocalDateTime ticketOpenTime) {
        this.performance = performance;
        this.performanceTime = performanceTime;
        this.ticketOpenTime = ticketOpenTime;
    }

    public Long getId() { return id; }
    public Performance getPerformance() { return performance; }
    public LocalDateTime getPerformanceTime() { return performanceTime; }
    public LocalDateTime getTicketOpenTime() { return ticketOpenTime; }

    public void update(Performance performance, LocalDateTime performanceTime, LocalDateTime ticketOpenTime) {
        if (performance != null) this.performance = performance;
        if (performanceTime != null) this.performanceTime = performanceTime;
        if (ticketOpenTime != null) this.ticketOpenTime = ticketOpenTime;
    }
}
