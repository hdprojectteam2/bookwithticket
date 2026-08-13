package com.example.bookwithticket.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdminOrderResponse {
	private final Long orderId;
	private final Long memberId;
	private final String orderNumber;
	private final LocalDateTime orderedAt;
	private final int totalPrice;
	private final String orderStatus;
	private final String deliveryStatus;
	private final String receiverName;
	private final String phone;
	private final String zipCode;
	private final String address;
	private final String detailAddress;
	private final String deliveryRequest;
	private final String courier;
	private final String trackingNumber;
	private final List<AdminOrderItemResponse> items;
	private final Long refundId;
	private final String refundStatus;
	private final String refundReason;

	public AdminOrderResponse(Long orderId, Long memberId, String orderNumber, LocalDateTime orderedAt, int totalPrice,
			String orderStatus, String deliveryStatus, String receiverName, String phone, String zipCode,
			String address, String detailAddress, String deliveryRequest, String courier, String trackingNumber,
			List<AdminOrderItemResponse> items, Long refundId, String refundStatus, String refundReason) {

		this.orderId = orderId;
		this.memberId = memberId;
		this.orderNumber = orderNumber;
		this.orderedAt = orderedAt;
		this.totalPrice = totalPrice;
		this.orderStatus = orderStatus;
		this.deliveryStatus = deliveryStatus;

		this.receiverName = receiverName;
		this.phone = phone;
		this.zipCode = zipCode;
		this.address = address;
		this.detailAddress = detailAddress;
		this.deliveryRequest = deliveryRequest;

		this.courier = courier;
		this.trackingNumber = trackingNumber;

		this.items = items;

		this.refundId = refundId;
		this.refundStatus = refundStatus;
		this.refundReason = refundReason;
	}

	public Long getOrderId() {
		return orderId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public LocalDateTime getOrderedAt() {
		return orderedAt;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public String getPhone() {
		return phone;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getAddress() {
		return address;
	}

	public String getDetailAddress() {
		return detailAddress;
	}

	public String getDeliveryRequest() {
		return deliveryRequest;
	}

	public String getCourier() {
		return courier;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public List<AdminOrderItemResponse> getItems() {
		return items;
	}

	public Long getRefundId() {
		return refundId;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public String getRefundReason() {
		return refundReason;
	}
}