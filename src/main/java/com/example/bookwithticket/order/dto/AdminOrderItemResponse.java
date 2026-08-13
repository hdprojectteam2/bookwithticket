package com.example.bookwithticket.order.dto;

public class AdminOrderItemResponse {

	private final Long orderItemId;
	private final String bookTitle;
	private final int price;
	private final int quantity;
	private final int totalPrice;

	public AdminOrderItemResponse(Long orderItemId, String bookTitle, int price, int quantity, int totalPrice) {
		this.orderItemId = orderItemId;
		this.bookTitle = bookTitle;
		this.price = price;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
	}

	public Long getOrderItemId() {
		return orderItemId;
	}

	public String getBookTitle() {
		return bookTitle;
	}

	public int getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getTotalPrice() {
		return totalPrice;
	}
}