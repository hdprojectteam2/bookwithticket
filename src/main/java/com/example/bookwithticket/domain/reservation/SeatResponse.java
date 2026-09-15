package com.example.bookwithticket.domain.reservation;

import java.time.LocalDateTime;

public record SeatResponse(
    Long id,
    Long scheduleId,
    String seatNumber,
    int price,
    String status,
    LocalDateTime holdExpiresAt,
    Long reservationId
) {
    public static SeatResponse from(Seat s) {
        return new SeatResponse(
            s.getId(),
            s.getSchedule().getId(),
            s.getSeatNumber(),
            s.getPrice(),
            s.getStatus().name(),
            null,
            null
        );
    }

    public static SeatResponse from(Seat s, LocalDateTime holdExpiresAt, Long reservationId) {
        return new SeatResponse(
            s.getId(),
            s.getSchedule().getId(),
            s.getSeatNumber(),
            s.getPrice(),
            s.getStatus().name(),
            holdExpiresAt,
            reservationId
        );
    }
}
