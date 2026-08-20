package com.example.bookwithticket.order.dto;

public class OrderPreviewResponse {

	private int totalQuantity;

	private int totalPrice;

	public OrderPreviewResponse(int totalQuantity, int totalPrice) {

		this.totalQuantity = totalQuantity;

		this.totalPrice = totalPrice;
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}

	public int getTotalPrice() {
		return totalPrice;
	}
}