package com.example.bookwithticket.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookwithticket.domain.reservation.Reservation;
import com.example.bookwithticket.domain.reservation.ReservationRepository;
import com.example.bookwithticket.domain.reservation.ReservationStatus;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPageItemDto;
import com.example.bookwithticket.order.service.OrderService;
import com.example.bookwithticket.payment.dto.PaymentCheckoutResponse;
import com.example.bookwithticket.payment.dto.PaymentConfirmRequest;
import com.example.bookwithticket.payment.dto.PaymentConfirmResponse;
import com.example.bookwithticket.payment.dto.PaymentFailureRequest;
import com.example.bookwithticket.payment.service.PaymentService;

@RestController
public class PaymentApiController {
	private final PaymentService paymentService;
	private final MemberService memberService;
	private final OrderService orderService;
	private final ReservationRepository reservationRepository;
	private final String clientKey;

	public PaymentApiController(PaymentService paymentService, MemberService memberService, OrderService orderService,
			ReservationRepository reservationRepository, @Value("${toss.payments.client-key}") String clientKey) {

		this.paymentService = paymentService;
		this.memberService = memberService;
		this.orderService = orderService;
		this.reservationRepository = reservationRepository;
		this.clientKey = clientKey;
	}

	private Long getCurrentMemberId(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("로그인이 필요합니다.");
		}

		String email = authentication.getName();

		Member member = memberService.findMyInfo(email);

		return member.getId();
	}

	@GetMapping("/api/payments/checkout")
	public ResponseEntity<PaymentCheckoutResponse> getCheckoutInfo(
			@RequestParam(name = "orderNumber") String orderNumber, Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		if (orderNumber == null || orderNumber.isBlank()) {

			throw new IllegalArgumentException("주문번호 또는 예매번호가 없습니다.");
		}

		if (orderNumber.startsWith("B")) {

			return getBookCheckoutInfo(memberId, orderNumber);
		}

		return getPerformanceCheckoutInfo(memberId, orderNumber);
	}

	private ResponseEntity<PaymentCheckoutResponse> getBookCheckoutInfo(Long memberId, String orderNumber) {

		OrderPageDto order = orderService.findPendingOrder(memberId, orderNumber);

		if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {

			throw new IllegalArgumentException("주문 상품이 없습니다.");
		}

		if (order.getTotalPrice() <= 0) {

			throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
		}

		String orderName = createOrderName(order);

		PaymentCheckoutResponse response = new PaymentCheckoutResponse(order.getOrderNumber(), orderName,
				order.getTotalPrice(), clientKey, null);

		return ResponseEntity.ok(response);
	}

	private ResponseEntity<PaymentCheckoutResponse> getPerformanceCheckoutInfo(Long memberId, String orderNumber) {

		Long reservationId;

		try {

			reservationId = Long.parseLong(orderNumber);

		} catch (NumberFormatException e) {

			throw new IllegalArgumentException("올바르지 않은 예매 ID입니다.");
		}

		Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
				.orElseThrow(() -> new IllegalArgumentException("결제할 수 없는 공연 예매입니다."));

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

		String paymentOrderId = "PERF_" + reservation.getId();

		PaymentCheckoutResponse response = new PaymentCheckoutResponse(paymentOrderId, orderName,
				reservation.getTotalPrice(), clientKey, reservation.getSeat().getSeatNumber());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/payments")
	public ResponseEntity<PaymentConfirmResponse> confirmPayment(@RequestBody PaymentConfirmRequest request,
			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		PaymentConfirmResponse response = paymentService.confirmPayment(memberId, request);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/payments/fail")
	public ResponseEntity<Void> savePaymentFailure(@RequestBody PaymentFailureRequest request,
			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		paymentService.savePaymentFailure(memberId, request);

		return ResponseEntity.ok().build();
	}

	private String createOrderName(OrderPageDto order) {

		OrderPageItemDto firstItem = order.getOrderItems().get(0);

		String firstBookTitle = firstItem.getBookTitle();

		int otherItemCount = order.getOrderItems().size() - 1;

		if (otherItemCount == 0) {

			return firstBookTitle;
		}

		int maxTitleLength = 15;

		if (firstBookTitle.length() > maxTitleLength) {

			firstBookTitle = firstBookTitle.substring(0, maxTitleLength) + "...";
		}

		return firstBookTitle + " 외 " + otherItemCount + "건";
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<PaymentErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {

		PaymentErrorResponse response = new PaymentErrorResponse("PAYMENT_CONFIRM_FAILED", exception.getMessage());

		return ResponseEntity.badRequest().body(response);
	}
}