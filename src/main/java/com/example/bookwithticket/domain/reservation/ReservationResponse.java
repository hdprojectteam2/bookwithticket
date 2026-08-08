package com.example.bookwithticket.domain.reservation;

import java.time.LocalDateTime;

public record ReservationResponse(
    Long id,
    Long scheduleId,
    Long seatId,
    String seatNumber,
    int price,
    String status,
    LocalDateTime holdExpiresAt
) {
	//35. 필요한 정보를 담고 돌아감
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
            r.getId(),
            r.getSchedule().getId(),
            r.getSeat().getId(),
            r.getSeat().getSeatNumber(),
            r.getTotalPrice(),
            r.getStatus().name(),
            r.getHoldExpiresAt()
        );
    }
}
