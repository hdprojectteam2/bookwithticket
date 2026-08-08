package com.example.bookwithticket.cart.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.bookwithticket.cart.service.CartService;

@Component
public class CartExpiredScheduler {
	private final CartService cartService;
	
	public CartExpiredScheduler(CartService cartService) {
		this.cartService = cartService;
	}
	
	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void expiredCarts() {
		cartService.deleteExpiredCarts();
	}
	
	
}
