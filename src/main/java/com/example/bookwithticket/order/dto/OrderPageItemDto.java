package com.example.bookwithticket.order.dto;

import com.example.bookwithticket.order.entity.BookOrderItemEntity;

public class OrderPageItemDto {

    private final Long orderItemId;
    private final Long bookId;

    private final String bookTitle;
    private final String thumbnail;

    private final int price;
    private final int quantity;
    private final int totalPrice;

    public OrderPageItemDto(BookOrderItemEntity orderItem) {
        this.orderItemId = orderItem.getId();
        this.bookId = orderItem.getBook().getId();

        this.bookTitle = orderItem.getBookTitleSnapshot();
        this.thumbnail = orderItem.getBook().getThumbnail();

        this.price = orderItem.getPriceSnapshot();
        this.quantity = orderItem.getQuantity();
        this.totalPrice = orderItem.getPriceSnapshot() * orderItem.getQuantity();
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getThumbnail() {
        return thumbnail;
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