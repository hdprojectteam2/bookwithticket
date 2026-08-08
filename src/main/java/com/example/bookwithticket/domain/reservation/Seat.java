package com.example.bookwithticket.domain.reservation;

import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "seat_number"}))
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    //n+1방지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id")
    private PerformanceSchedule schedule;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    protected Seat() {}

    public Seat(PerformanceSchedule schedule, String seatNumber, int price) {
        this.schedule = schedule;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    public Long getId() { return id; }
    public PerformanceSchedule getSchedule() { return schedule; }
    public String getSeatNumber() { return seatNumber; }
    public int getPrice() { return price; }
    public SeatStatus getStatus() { return status; }

    public void updateStatus(SeatStatus status) {
        this.status = status;
    }
}
