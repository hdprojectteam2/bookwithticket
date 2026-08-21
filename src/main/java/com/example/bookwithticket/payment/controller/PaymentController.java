package com.example.bookwithticket.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

	@GetMapping("/payments/checkout")
	public String checkoutPage(@RequestParam(name = "orderNumber") String orderNumber, Model model) {

		if (orderNumber == null || orderNumber.isBlank()) {
			throw new IllegalArgumentException("주문번호 또는 예매번호가 없습니다.");
		}

		model.addAttribute("orderNumber", orderNumber);

		if (orderNumber.startsWith("B")) {
			return "payments/checkout";
		}

		try {
			Long.parseLong(orderNumber);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("올바르지 않은 주문번호 또는 예매번호입니다.");
		}

		return "payments/performanceCheckout";
	}

	@GetMapping("/payments/success")
	public String successPage() {
		return "payments/success";
	}

	@GetMapping("/payments/fail")
	public String failPage() {
		return "payments/fail";
	}
}