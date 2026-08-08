package com.example.bookwithticket.history.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.bookwithticket.history.dto.BookOrderHistoryDto;
import com.example.bookwithticket.history.dto.PerformanceHistoryDto;
import com.example.bookwithticket.history.service.BookOrderHistoryService;
import com.example.bookwithticket.history.service.PerformanceHistoryService;

@Controller
public class HistoryController {

	private final BookOrderHistoryService bookOrderHistoryService; 
	private final PerformanceHistoryService performanceHistoryService;
	
	public HistoryController(BookOrderHistoryService bookOrderHistoryService, PerformanceHistoryService performanceHistoryService) {
		this.bookOrderHistoryService = bookOrderHistoryService;
		this.performanceHistoryService = performanceHistoryService;
	}
	
	// 임시 ID 1
    private Long getCurrentMemberId() {
        return 1L;
    }
	
	@GetMapping("/history")
	public String historyPage(Model model) {
		
		Long memberId = getCurrentMemberId();
		
		/* 도서 구매 내역 */
		List<BookOrderHistoryDto> historyItems = bookOrderHistoryService.findOrderHistory(memberId);
		
		model.addAttribute("historyItems", historyItems);
		
		/* 공연 구매 내역 */
		List<PerformanceHistoryDto> performanceHistoryItems = performanceHistoryService.findPerformanceHistory(memberId);

		model.addAttribute("performanceHistoryItems", performanceHistoryItems);
		
		return "history/orderHistory";
	}
}
