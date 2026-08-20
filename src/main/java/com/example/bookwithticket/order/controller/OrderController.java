package com.example.bookwithticket.order.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import com.example.bookwithticket.order.dto.AdminOrderResponse;
import com.example.bookwithticket.order.dto.AdminReservationResponse;
import com.example.bookwithticket.order.dto.DeliveryStatusRequest;
import com.example.bookwithticket.order.dto.OrderCreateRequest;
import com.example.bookwithticket.order.dto.OrderMemberInfoResponse;
import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPreparedRequest;
import com.example.bookwithticket.order.dto.OrderPreparedResponse;
import com.example.bookwithticket.order.dto.OrderPreviewResponse;
import com.example.bookwithticket.order.dto.ShippingRequest;
import com.example.bookwithticket.order.service.AdminReservationService;
import com.example.bookwithticket.order.service.OrderService;

@Controller
public class OrderController {

	private final OrderService orderService;
	private final MemberService memberService;
	private final AdminReservationService adminReservationService;

	public OrderController(OrderService orderService, MemberService memberService,
			AdminReservationService adminReservationService) {
		this.orderService = orderService;
		this.memberService = memberService;
		this.adminReservationService = adminReservationService;
	}

	private Long getCurrentMemberId(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("로그인이 필요합니다.");
		}

		String email = authentication.getName();

		Member member = memberService.findMyInfo(email);

		return member.getId();
	}

	@GetMapping("/order")
	public String orderPage() {
		return "order/order";
	}
	
	@ResponseBody
	@GetMapping("/api/orders/member-info")
	public ResponseEntity<OrderMemberInfoResponse> getMemberInfo(
	        Authentication authentication) {

	    if (authentication == null || !authentication.isAuthenticated()) {
	        throw new IllegalArgumentException("로그인이 필요합니다.");
	    }

	    String email = authentication.getName();

	    Member member = memberService.findMyInfo(email);

	    OrderMemberInfoResponse response =
	            new OrderMemberInfoResponse(
	                    member.getName(),
	                    member.getPhone(),
	                    member.getZipcode(),
	                    member.getAddress(),
	                    member.getDetailAddress()
	            );

	    return ResponseEntity.ok(response);
	}

	@ResponseBody
	@PostMapping("/api/orders")
	public ResponseEntity<OrderPreparedResponse> createOrder(@RequestBody OrderCreateRequest request,
			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		OrderPreparedResponse response = orderService.createOrder(memberId, request);

		return ResponseEntity.ok(response);
	}

	@ResponseBody
	@GetMapping("/api/orders/{orderNumber}")
	public ResponseEntity<OrderPageDto> getOrder(@PathVariable(name = "orderNumber") String orderNumber,
			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		OrderPageDto order = orderService.findPendingOrder(memberId, orderNumber);

		return ResponseEntity.ok(order);
	}

	/* 예외 처리 */
	@ResponseBody
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.badRequest().body(exception.getMessage());
	}


	@PostMapping("/api/orders/{orderNumber}/cancel")
	public ResponseEntity<Void> cancelOrder(@PathVariable(name = "orderNumber") String orderNumber,
			Authentication authentication) {
		Long memberId = getCurrentMemberId(authentication);

		orderService.cancelOrder(memberId, orderNumber);

		return ResponseEntity.ok().build();
	}

	@ResponseBody
	@GetMapping("/api/orders/{orderNumber}/completed")
	public ResponseEntity<OrderPageDto> getCompletedOrder(@PathVariable(name = "orderNumber") String orderNumber,

			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		OrderPageDto order = orderService.findCompletedOrder(memberId, orderNumber);

		return ResponseEntity.ok(order);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/orders")
	public String adminOrdersPage() {
		return "order/adminOrder";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/api/admin/check")
	@ResponseBody
	public ResponseEntity<Void> checkAdmin() {

		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/api/admin/orders")
	public ResponseEntity<List<AdminOrderResponse>> getOrders() {

		return ResponseEntity.ok(orderService.findAdminOrders());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/api/admin/orders/{orderNumber}/tracking")
	public ResponseEntity<String> updateTrackingInfo(

			@PathVariable(name = "orderNumber") String orderNumber,

			@RequestBody ShippingRequest request) {

		orderService.updateTrackingInfo(orderNumber, request);

		return ResponseEntity.ok("배송 정보가 저장되었습니다.");
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/api/admin/orders/{orderNumber}/delivery-status")
	public ResponseEntity<String> updateDeliveryStatus(

			@PathVariable(name = "orderNumber") String orderNumber,

			@RequestBody DeliveryStatusRequest request) {

		orderService.updateDeliveryStatus(orderNumber, request);

		return ResponseEntity.ok("배송 상태가 변경되었습니다.");
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/api/admin/reservations")
	public ResponseEntity<List<AdminReservationResponse>> getAdminReservations() {

		return ResponseEntity.ok(adminReservationService.findReservations());
	}

	@ResponseBody
	@PostMapping("/api/orders/preview")
	public ResponseEntity<OrderPreviewResponse> previewOrder(

			@RequestBody OrderPreparedRequest request,

			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		OrderPreviewResponse response = orderService.previewOrder(memberId, request);

		return ResponseEntity.ok(response);
	}
}