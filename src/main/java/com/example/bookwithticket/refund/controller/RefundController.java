package com.example.bookwithticket.refund.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import com.example.bookwithticket.refund.dto.RefundRequest;
import com.example.bookwithticket.refund.dto.RefundResponse;
import com.example.bookwithticket.refund.service.RefundService;

@RestController
public class RefundController {
	private final RefundService refundService;
	private final MemberService memberService;

	public RefundController(RefundService refundService, MemberService memberService) {
		this.refundService = refundService;
		this.memberService = memberService;
	}

	private Long getCurrentMemberId(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("로그인이 필요합니다.");
		}

		String email = authentication.getName();

		Member member = memberService.findMyInfo(email);

		return member.getId();
	}

	@PostMapping("/api/payments/{orderNumber}/refund")
	public ResponseEntity<RefundResponse> requestRefund(@PathVariable(name = "orderNumber") String orderNumber,
			@RequestBody RefundRequest request, Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		RefundResponse response;

		if (orderNumber.startsWith("B")) {

			response = refundService.requestBookRefund(memberId, orderNumber, request.getReason(), request.getReturnMethod());

		} else {

			response = refundService.requestPerformanceRefund(memberId, orderNumber, request.getReason());
		}

		return ResponseEntity.ok(response);
	}

	/* 관리자 - 환불 승인 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/api/admin/refunds/{refundId}/approve")
	public ResponseEntity<RefundResponse> approveRefund(@PathVariable(name = "refundId") Long refundId,
			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		RefundResponse response = refundService.approveBookRefund(memberId, refundId);

		return ResponseEntity.ok(response);
	}

	/* 관리자 - 환불 거절 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/api/admin/refunds/{refundId}/reject")
	public ResponseEntity<RefundResponse> rejectRefund(@PathVariable(name = "refundId") Long refundId,
			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		RefundResponse response = refundService.rejectBookRefund(memberId, refundId);

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/api/admin/orders/{orderNumber}/force-refund")
	public ResponseEntity<RefundResponse> forceBookRefund(@PathVariable("orderNumber") String orderNumber,
			Authentication authentication) {
		Long adminId = getCurrentMemberId(authentication);

		RefundResponse response = refundService.forceBookRefund(adminId, orderNumber);

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/api/admin/reservations/{reservationId}/force-refund")
	public ResponseEntity<RefundResponse> forcePerformanceRefund(
			@PathVariable(name = "reservationId") Long reservationId,
			Authentication authentication) {
		Long adminId = getCurrentMemberId(authentication);

		RefundResponse response = refundService.forcePerformanceRefund(adminId, reservationId);

		return ResponseEntity.ok(response);
	}
}
