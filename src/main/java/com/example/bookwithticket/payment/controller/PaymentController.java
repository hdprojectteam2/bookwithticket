package com.example.bookwithticket.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bookwithticket.domain.reservation.Reservation;
import com.example.bookwithticket.domain.reservation.ReservationRepository;
import com.example.bookwithticket.domain.reservation.ReservationStatus;
import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPageItemDto;
import com.example.bookwithticket.order.service.OrderService;

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
        
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("주문번호 또는 예매번호가 없습니다.");
        }

        if (orderNumber.startsWith("B")) {
            return bookCheckoutPage(memberId, orderNumber, model);
        }

        try {
            Long.parseLong(orderNumber);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("올바르지 않은 주문번호 또는 예매번호입니다.");
        }

        return performanceCheckoutPage(memberId, orderNumber, model);

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
    
    private String performanceCheckoutPage(Long memberId, String orderNumber, Model model) {
    	
<<<<<<< HEAD
    	Long reservationId = parseReservationId(orderNumber);
=======
    	Long reservationId;

        try {
            reservationId = Long.parseLong(orderNumber);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("올바르지 않은 예매 ID입니다.");
        }
>>>>>>> feature/cart

    	
    	Reservation reservation =
                reservationRepository
                        .findByIdAndMemberId(
                        		reservationId,
                                memberId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException("결제할 수 없는 공연 예매입니다.")
                        );

    	 if (reservation.getStatus() != ReservationStatus.HELD) {
    	        throw new IllegalArgumentException("결제 대기 상태의 공연 예매가 아닙니다.");
    	    }

    	    if (reservation.isExpired()) {
    	        throw new IllegalArgumentException("좌석 선점 시간이 만료되었습니다.");
    	    }
    	
        if (reservation.getTotalPrice() <= 0) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        String orderName = reservation.getSchedule().getPerformance().getTitle();
<<<<<<< HEAD

        model.addAttribute("reservation", reservation);
        model.addAttribute("paymentOrderId", orderNumber);
=======
        
        
        model.addAttribute("reservation", reservation);
        model.addAttribute("paymentOrderId", "PERF_" + reservation.getId());
>>>>>>> feature/cart
        model.addAttribute("orderName", orderName);
        model.addAttribute("tossClientKey", clientKey);

        return "payments/performanceCheckout";
    }
    
    private Long parseReservationId(String orderNumber) {

        if (orderNumber == null || !orderNumber.startsWith("R")) {

            throw new IllegalArgumentException("올바르지 않은 예매번호입니다.");
        }

        try {

            return Long.parseLong(orderNumber.substring(1));

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("올바르지 않은 예매번호입니다.");
        }
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