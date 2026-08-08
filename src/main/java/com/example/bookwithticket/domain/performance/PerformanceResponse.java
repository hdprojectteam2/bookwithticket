package com.example.bookwithticket.domain.performance;

public record PerformanceResponse(
    Long id,
    String title,
    String category,
    String venue,
    String posterUrl,
    int runtimeMinutes,
    String description,
    Long originalBookId
) {
	
	// 7. 필요한 정보들 담고 
    public static PerformanceResponse from(Performance p) {
        return new PerformanceResponse(
            p.getId(),
            p.getTitle(),
            p.getCategory().name(),
            p.getVenue(),
            p.getPosterUrl(),
            p.getRuntimeMinutes(),
            p.getDescription(),
            p.getOriginalBookId()
        );
    }
}
