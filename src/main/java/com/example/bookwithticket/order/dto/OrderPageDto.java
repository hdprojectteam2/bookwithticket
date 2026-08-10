package com.example.bookwithticket.order.dto;

import java.util.List;

import com.example.bookwithticket.order.entity.BookOrderEntity;
import com.example.bookwithticket.order.entity.BookOrderItemEntity;

public class OrderPageDto {

    private final Long orderId;
    private final String orderNumber;
    private final String orderStatus;

    private final List<OrderPageItemDto> orderItems;

    private final int totalQuantity;
    private final int totalPrice;

    public OrderPageDto(BookOrderEntity order) {
        this.orderId = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.orderStatus = order.getOrderStatus().name();

        this.orderItems = order.getOrderItems()
                .stream()
                .map(OrderPageItemDto::new)
                .toList();

        this.totalQuantity = order.getOrderItems()
                .stream()
                .mapToInt(
                        BookOrderItemEntity::getQuantity
                )
                .sum();

        this.totalPrice = order.getTotalPrice();
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public List<OrderPageItemDto> getOrderItems() {
        return orderItems;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getTotalPrice() {
        return totalPrice;
    }
}