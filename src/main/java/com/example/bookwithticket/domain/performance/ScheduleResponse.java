package com.example.bookwithticket.domain.performance;

import java.time.LocalDateTime;

public record ScheduleResponse(
    Long id,
    Long performanceId,
    LocalDateTime performanceTime,
    LocalDateTime ticketOpenTime
) {
    public static ScheduleResponse from(PerformanceSchedule s) {
        return new ScheduleResponse(
            s.getId(),
            s.getPerformance().getId(),
            s.getPerformanceTime(),
            s.getTicketOpenTime()
        );
    }
}
