package com.example.bookwithticket.history.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bookwithticket.history.dto.BookOrderHistoryDto;
import com.example.bookwithticket.history.dto.PerformanceHistoryDto;
import com.example.bookwithticket.history.service.BookOrderHistoryService;
import com.example.bookwithticket.history.service.PerformanceHistoryService;

@Controller
public class HistoryController {

	private final BookOrderHistoryService bookOrderHistoryService;
	private final PerformanceHistoryService performanceHistoryService;

	public HistoryController(BookOrderHistoryService bookOrderHistoryService,
			PerformanceHistoryService performanceHistoryService) {
		this.bookOrderHistoryService = bookOrderHistoryService;
		this.performanceHistoryService = performanceHistoryService;
	}

	// 임시 ID 1
	private Long getCurrentMemberId() {
		return 1L;
	}

	// 구매내역 페이지
	@GetMapping("/history")
	public String historyPage() {

		return "history/orderHistory";
	}

	// 도서 구매내역 API
	@ResponseBody
	@GetMapping("/api/history/books")
	public ResponseEntity<List<BookOrderHistoryDto>> bookHistory() {

		Long memberId = getCurrentMemberId();

		List<BookOrderHistoryDto> historyItems = bookOrderHistoryService.findOrderHistory(memberId);

		return ResponseEntity.ok(historyItems);
	}

	// 공연 구매내역 API
	@ResponseBody
	@GetMapping("/api/history/performances")
	public ResponseEntity<List<PerformanceHistoryDto>> performanceHistory() {

		Long memberId = getCurrentMemberId();

		List<PerformanceHistoryDto> performanceHistoryItems = performanceHistoryService
				.findPerformanceHistory(memberId);

		return ResponseEntity.ok(performanceHistoryItems);
	}
}