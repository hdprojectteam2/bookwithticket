package com.example.bookwithticket.history.service;

import java.util.List;

import com.example.bookwithticket.history.dto.BookOrderHistoryDto;

public interface BookOrderHistoryService {
	
	List<BookOrderHistoryDto> findOrderHistory(Long memberId);
}
