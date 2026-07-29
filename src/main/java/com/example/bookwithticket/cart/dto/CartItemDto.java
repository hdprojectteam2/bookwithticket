package com.example.bookwithticket.cart.dto;

public class CartItemDto {

    private final Long cartItemId;
    private final Long bookId;
    private final String bookTitle;
    private final int price;
    private final int quantity;
    private final int totalPrice;

    

    public CartItemDto(Long cartItemId, Long bookId, String bookTitle, int price, int quantity) {
		this.cartItemId = cartItemId;
		this.bookId = bookId;
		this.bookTitle = bookTitle;
		this.price = price;
		this.quantity = quantity;
		this.totalPrice = price*quantity;
	}



	public Long getCartItemId() {
		return cartItemId;
	}



	public Long getBookId() {
		return bookId;
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