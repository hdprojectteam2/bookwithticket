package com.example.bookwithticket.domain.performance;

public record PerformanceResponse(
    Long id,
    String title,
    String category,
    String venue,
    String posterUrl,
    int runtimeMinutes,
    String description,
    Long originalBookId,
    boolean active,
    int seatscale
) {
    public static PerformanceResponse from(Performance p) {
        return new PerformanceResponse(
            p.getId(),
            p.getTitle(),
            p.getCategory().name(),
            p.getVenue(),
            p.getPosterUrl(),
            p.getRuntimeMinutes(),
            p.getDescription(),
            p.getOriginalBookId(),
            p.isActive(),
            p.getSeatscale()
        );
    }
}
