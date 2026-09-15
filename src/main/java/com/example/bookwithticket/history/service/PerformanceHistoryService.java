package com.example.bookwithticket.history.service;

import java.util.List;

import com.example.bookwithticket.history.dto.PerformanceHistoryDto;

public interface PerformanceHistoryService {
	List<PerformanceHistoryDto> findPerformanceHistory(Long memberId);

	PerformanceHistoryDto findReservationHistoryDetail(Long memberId, Long reservationId);
}
