package com.example.bookwithticket.order.dto;

public class OrderPreviewResponse {

	private int totalQuantity;
	
	private int originalPrice;

	private int totalPrice;

	public OrderPreviewResponse(int totalQuantity, int originalPrice, int totalPrice) {

		this.totalQuantity = totalQuantity;
		this.originalPrice = originalPrice;
		this.totalPrice = totalPrice;
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}
	
	public int getOriginalPrice() {
	    return originalPrice;
	}

	public int getTotalPrice() {
		return totalPrice;
	}
}