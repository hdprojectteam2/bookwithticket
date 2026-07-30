package com.example.bookwithticket.order.dto;

public class OrderPreparedResponse {
	private final Long orderId;
    private final String orderNumber;
    private final int totalPrice;
    private final String orderStatus;

    public OrderPreparedResponse(
            Long orderId,
            String orderNumber,
            int totalPrice,
            String orderStatus
    ) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
