package com.example.bookwithticket.history.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BookOrderHistoryDto {

	private final String orderNumber;
    private final LocalDateTime orderedAt;
    private final String deliveryStatus;
    private final String deliveryStatusCode;
    private final String orderStatusCode;
    private final String refundStatus;
    private final String refundStatusCode;
    private final int totalPrice;
    private final List<BookOrderHistoryItemDto> orderItems;
    
	public BookOrderHistoryDto(String orderNumber, LocalDateTime orderedAt, String deliveryStatus, String deliveryStatusCode, String orderStatusCode, String refundStatus, String refundStatusCode, int totalPrice,
			List<BookOrderHistoryItemDto> orderItems) {
		this.orderNumber = orderNumber;
		this.orderedAt = orderedAt;
		this.deliveryStatus = deliveryStatus;
		this.deliveryStatusCode = deliveryStatusCode;
		this.orderStatusCode = orderStatusCode;
		this.refundStatus = refundStatus;
		this.refundStatusCode = refundStatusCode;
		this.totalPrice = totalPrice;
		this.orderItems = orderItems;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public LocalDateTime getOrderedAt() {
		return orderedAt;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}
	
	public String getDeliveryStatusCode() {
		return deliveryStatusCode;
	}
	
	public String getOrderStatusCode() {
		return orderStatusCode;
	}
	
	public String getRefundStatus() {
        return refundStatus;
    }

    public String getRefundStatusCode() {
        return refundStatusCode;
    }

	public int getTotalPrice() {
		return totalPrice;
	}

	public List<BookOrderHistoryItemDto> getOrderItems() {
		return orderItems;
	}


}
