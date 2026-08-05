package com.example.bookwithticket.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPageItemDto;
import com.example.bookwithticket.order.service.OrderService;
import com.example.bookwithticket.reservation.entity.ReservationEntity;
import com.example.bookwithticket.reservation.entity.ReservationStatus;
import com.example.bookwithticket.reservation.repository.ReservationRepository;

@Controller
public class PaymentController {

    private final OrderService orderService;
    private final ReservationRepository reservationRepository;
    private final String clientKey;

    public PaymentController(
    		OrderService orderService, 
    		ReservationRepository reservationRepository,
    		@Value("${toss.payments.client-key}")
            String clientKey
            ) {
        this.orderService = orderService;
        this.reservationRepository = reservationRepository;
        this.clientKey = clientKey;
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

        if (orderNumber.startsWith("B")) {
            return bookCheckoutPage(memberId, orderNumber, model);
        }

        if (orderNumber.startsWith("R")) {
            return performanceCheckoutPage(memberId, orderNumber, model);
        }

        throw new IllegalArgumentException(
                "올바르지 않은 주문번호 또는 예매번호입니다."
        );
    }
    
    private String bookCheckoutPage(Long memberId, String orderNumber, Model model) {
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
        model.addAttribute("tossClientKey", clientKey);
        
        return "payments/checkout";
    }
    
    private String performanceCheckoutPage(Long memberId, String reservationNumber, Model model) {
        ReservationEntity reservation =
                reservationRepository
                        .findByReservationNumberAndMemberIdAndStatus(
                                reservationNumber,
                                memberId,
                                ReservationStatus.PAYMENT_PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException("결제할 수 없는 공연 예매입니다.")
                        );

        if (reservation.getTotalPrice() <= 0) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        String orderName = reservation.getPerformanceSchedule().getPerformance().getTitle();

        model.addAttribute("reservation", reservation);
        model.addAttribute("orderName", orderName);
        model.addAttribute("tossClientKey", clientKey);

        return "payments/performanceCheckout";
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