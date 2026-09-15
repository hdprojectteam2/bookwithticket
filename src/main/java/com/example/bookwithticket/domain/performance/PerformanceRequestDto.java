package com.example.bookwithticket.domain.performance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PerformanceRequestDto(
    @NotBlank(message = "공연 제목 필수") String title,
    @NotNull(message = "카테고리 필수.") PerformanceCategory category,
    @NotBlank(message = "공연장 필수") String venue,
    String posterUrl,
    int runtimeMinutes,
    String description,
    Long originalBookId
) {}
