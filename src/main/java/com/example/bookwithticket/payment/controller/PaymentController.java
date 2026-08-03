package com.example.bookwithticket.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPageItemDto;
import com.example.bookwithticket.order.service.OrderService;

@Controller
public class PaymentController {

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    /* 임시 회원 ID */
    private Long getCurrentMemberId() {
        return 1L;
    }

    @GetMapping("/payments/checkout")
    public String checkoutPage(
            @RequestParam(name = "orderNumber")
            String orderNumber,
            Model model
    ) {
        Long memberId = getCurrentMemberId();

        OrderPageDto order = orderService.findPendingOrder(memberId, orderNumber);

        if (order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("주문 상품이 없습니다.");
        }

        if (order.getTotalPrice() <= 0) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        String orderName = createOrderName(order);

        model.addAttribute("order", order);
        model.addAttribute("orderName", orderName);

        return "payments/checkout";
    }

    private String createOrderName(OrderPageDto order) {
        OrderPageItemDto firstItem = order.getOrderItems().get(0);

        String firstBookTitle = firstItem.getBookTitle();
        
        int otherItemCount = order.getOrderItems().size() - 1;

        if (otherItemCount == 0) {
            return firstBookTitle;
        }
        
        int maxTitleLength = 15;

        if (firstItem.getBookTitle().length() > maxTitleLength) {
        	firstBookTitle = firstItem.getBookTitle().substring(0, maxTitleLength) + "...";
        }

        return firstBookTitle + " 외 " + otherItemCount + "건";
    }

    @GetMapping("/payments/success")
    public String successPage() {
        return "payments/success";
    }

    @GetMapping("/payments/fail")
    public String failPage() {
        return "payments/fail";
    }
}