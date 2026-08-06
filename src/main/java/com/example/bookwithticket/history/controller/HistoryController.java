package com.example.bookwithticket.history.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.bookwithticket.history.dto.BookOrderHistoryDto;
import com.example.bookwithticket.history.service.BookOrderHistoryService;

@Controller
public class HistoryController {

	private final BookOrderHistoryService bookOrderHistoryService; 
	
	public HistoryController(BookOrderHistoryService bookOrderHistoryService) {
		this.bookOrderHistoryService = bookOrderHistoryService;
	}
	
	// 임시 ID 1
    private Long getCurrentMemberId() {
        return 1L;
    }
	
	@GetMapping("/history")
	public String historyPage(Model model) {
		
		Long memberId = getCurrentMemberId();
		
		/* 도서 구매내역 */
		List<BookOrderHistoryDto> historyItems = bookOrderHistoryService.findOrderHistory(memberId);
		
		model.addAttribute("historyItems", historyItems);
		
		return "history/orderHistory";
	}
}
