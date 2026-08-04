package com.example.bookwithticket.order.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.bookwithticket.order.service.OrderService;

@Component
public class OrderScheduler {

    private final OrderService orderService;

    public OrderScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    // 1분마다 실행
    @Scheduled(fixedDelay = 60000)
    public void cancelExpiredOrders() {
        orderService.cancelExpiredOrders();
    }
}