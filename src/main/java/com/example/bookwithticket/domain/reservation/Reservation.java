package com.example.bookwithticket.domain.reservation;

import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id")
    private PerformanceSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.HELD;

    private LocalDateTime holdExpiresAt;

    @Column(nullable = false)
    private int totalPrice;

    protected Reservation() {}

    public Reservation(Long memberId, PerformanceSchedule schedule, Seat seat, int totalPrice, int holdMinutes) {
        this.memberId = memberId;
        this.schedule = schedule;
        this.seat = seat;
        this.totalPrice = totalPrice;
        this.status = ReservationStatus.HELD;
        this.holdExpiresAt = LocalDateTime.now().plusMinutes(holdMinutes);
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public PerformanceSchedule getSchedule() { return schedule; }
    public Seat getSeat() { return seat; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }
    public int getTotalPrice() { return totalPrice; }

    public boolean isExpired() {
        return status == ReservationStatus.EXPIRED || (status == ReservationStatus.HELD && holdExpiresAt != null && LocalDateTime.now().isAfter(holdExpiresAt));
    }

    public void confirm() {
        if (isExpired()) {
            throw new IllegalStateException("선점 시간이 만료되었습니다.");
        }
        if (this.status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예매입니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예매입니다.");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }
}
