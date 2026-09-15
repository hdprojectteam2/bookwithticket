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

	private final String receiverName;
	private final String receiverPhone;
	private final String address;
	private final String paymentMethod;
	private final LocalDateTime paidAt;
	
	private String courier;
	private String trackingNumber;

	private final List<BookOrderHistoryItemDto> orderItems;

	public BookOrderHistoryDto(String orderNumber, LocalDateTime orderedAt, String deliveryStatus,
			String deliveryStatusCode, String orderStatusCode, String refundStatus, String refundStatusCode,
			int totalPrice, String receiverName, String receiverPhone, String address, String paymentMethod,
			LocalDateTime paidAt, List<BookOrderHistoryItemDto> orderItems, String courier, String trackingNumber) {

		this.orderNumber = orderNumber;
		this.orderedAt = orderedAt;
		this.deliveryStatus = deliveryStatus;
		this.deliveryStatusCode = deliveryStatusCode;
		this.orderStatusCode = orderStatusCode;
		this.refundStatus = refundStatus;
		this.refundStatusCode = refundStatusCode;
		this.totalPrice = totalPrice;
		this.receiverName = receiverName;
		this.receiverPhone = receiverPhone;
		this.address = address;
		this.paymentMethod = paymentMethod;
		this.paidAt = paidAt;
		this.orderItems = orderItems;
		this.courier = courier;
        this.trackingNumber = trackingNumber;
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

	public String getReceiverName() {
		return receiverName;
	}

	public String getReceiverPhone() {
		return receiverPhone;
	}

	public String getAddress() {
		return address;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public List<BookOrderHistoryItemDto> getOrderItems() {
		return orderItems;
	}
	
	public String getCourier() {
	    return courier;
	}

	public String getTrackingNumber() {
	    return trackingNumber;
	}
}