package com.example.bookwithticket.history.dto;

public class BookOrderHistoryItemDto {

    private final String bookTitle;
    private final String author;
    private final String publisher;
    private final String imageUrl;
    private final int unitPrice;
    private final int quantity;
    private final int totalPrice;

   

    public BookOrderHistoryItemDto(String bookTitle, String author, String publisher, String imageUrl, int unitPrice,
			int quantity, int totalPrice) {
		this.bookTitle = bookTitle;
		this.author = author;
		this.publisher = publisher;
		this.imageUrl = imageUrl;
		this.unitPrice = unitPrice;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
	}

	public String getBookTitle() {
        return bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getTotalPrice() {
        return unitPrice * quantity;
    }
}