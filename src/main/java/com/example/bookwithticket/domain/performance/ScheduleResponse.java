package com.example.bookwithticket.domain.performance;

import java.time.LocalDateTime;

public record ScheduleResponse(
    Long id,
    Long performanceId,
    LocalDateTime performanceTime,
    LocalDateTime ticketOpenTime,
    int totalSeats,
    int availableSeats
) {
    public static ScheduleResponse from(PerformanceSchedule s, int totalSeats, int availableSeats) {
        return new ScheduleResponse(
            s.getId(),
            s.getPerformance().getId(),
            s.getPerformanceTime(),
            s.getTicketOpenTime(),
            totalSeats,
            availableSeats
        );
    }

    public static ScheduleResponse from(PerformanceSchedule s) {
        return from(s, 0, 0);
    }
}
