package com.example.bookwithticket.order.dto;

import java.util.List;

public class OrderCreateRequest {

	private List<Long> cartItemIds;

	private String recipient;

	private String phone;

	private String zipcode;

	private String address;

	private String detailAddress;

	private String deliveryRequest;

	public List<Long> getCartItemIds() {
		return cartItemIds;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getPhone() {
		return phone;
	}

	public String getZipcode() {
		return zipcode;
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
}