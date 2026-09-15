package com.example.bookwithticket.domain.reservation;

import jakarta.validation.constraints.NotNull;

public record ReservationHoldRequest(
    @NotNull Long scheduleId,
    @NotNull Long seatId
) {}
