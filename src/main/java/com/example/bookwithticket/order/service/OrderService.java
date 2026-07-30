package com.example.bookwithticket.order.service;

import com.example.bookwithticket.order.dto.OrderPreparedRequest;
import com.example.bookwithticket.order.dto.OrderPreparedResponse;
import com.example.bookwithticket.order.dto.OrderPageDto;

public interface OrderService {

    OrderPreparedResponse prepareOrder(
            Long memberId,
            OrderPreparedRequest request
    );

    OrderPageDto findPendingOrder(
            Long memberId,
            String orderNumber
    );
}