package com.example.bookwithticket.order.dto;

import java.util.List;

public class OrderPreparedRequest {
	private List<Long> cartItemIds;

    public List<Long> getCartItemIds() {
        return cartItemIds;
    }

    public void setCartItemIds(List<Long> cartItemIds) {
        this.cartItemIds = cartItemIds;
    }
}
