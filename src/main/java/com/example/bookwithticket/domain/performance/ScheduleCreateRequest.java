package com.example.bookwithticket.domain.performance;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ScheduleCreateRequest(
    @NotNull(message = "공연 일시는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    LocalDateTime performanceTime,

    @NotNull(message = "티켓 오픈 일시는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    LocalDateTime ticketOpenTime,

    @Min(value = 1, message = "총 좌석 수는 최소 1개 이상이어야 합니다.")
    int totalSeats,

    @Min(value = 0, message = "좌석 가격은 0원 이상이어야 합니다.")
    int seatPrice
) {}
