package com.example.bookwithticket.order.dto;

public class ShippingRequest {

	private String courier;

	private String trackingNumber;

	public ShippingRequest() {
	}

	public String getCourier() {
		return courier;
	}

	public void setCourier(String courier) {
		this.courier = courier;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}
}