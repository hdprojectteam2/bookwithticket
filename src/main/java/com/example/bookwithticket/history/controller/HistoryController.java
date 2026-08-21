package com.example.bookwithticket.history.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bookwithticket.history.dto.BookOrderHistoryDto;
import com.example.bookwithticket.history.dto.PerformanceHistoryDto;
import com.example.bookwithticket.history.service.BookOrderHistoryService;
import com.example.bookwithticket.history.service.PerformanceHistoryService;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;

@Controller
public class HistoryController {

	private final BookOrderHistoryService bookOrderHistoryService;
	private final PerformanceHistoryService performanceHistoryService;
	private final MemberService memberService;

	public HistoryController(BookOrderHistoryService bookOrderHistoryService,
			PerformanceHistoryService performanceHistoryService, MemberService memberService) {
		this.bookOrderHistoryService = bookOrderHistoryService;
		this.performanceHistoryService = performanceHistoryService;
		this.memberService = memberService;
	}

	private Long getCurrentMemberId(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("로그인이 필요합니다.");
		}

		String email = authentication.getName();

		Member member = memberService.findMyInfo(email);

		return member.getId();
	}

	// 구매내역 페이지
	@GetMapping("/history")
	public String historyPage(Authentication authentication) {

		return "history/orderHistory";
	}

	// 도서 구매내역 API
	@ResponseBody
	@GetMapping("/api/history/books")
	public ResponseEntity<List<BookOrderHistoryDto>> bookHistory(Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		List<BookOrderHistoryDto> historyItems = bookOrderHistoryService.findOrderHistory(memberId);

		return ResponseEntity.ok(historyItems);
	}

	// 공연 구매내역 API
	@ResponseBody
	@GetMapping("/api/history/performances")
	public ResponseEntity<List<PerformanceHistoryDto>> performanceHistory(Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		List<PerformanceHistoryDto> performanceHistoryItems = performanceHistoryService
				.findPerformanceHistory(memberId);

		return ResponseEntity.ok(performanceHistoryItems);
	}

	@GetMapping("/api/history/books/{orderNumber}")
	public ResponseEntity<BookOrderHistoryDto> bookOrderDetail(@PathVariable(name = "orderNumber") String orderNumber,
			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		BookOrderHistoryDto response = bookOrderHistoryService.findOrderHistoryDetail(memberId, orderNumber);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/history/performances/{reservationId}")
	public ResponseEntity<PerformanceHistoryDto> performanceOrderDetail(
			@PathVariable(name = "reservationId") Long reservationId, Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		PerformanceHistoryDto response = performanceHistoryService.findReservationHistoryDetail(memberId,
				reservationId);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/history/detail")
	public String historyDetailPage() {
		return "history/historyDetail";
	}
}