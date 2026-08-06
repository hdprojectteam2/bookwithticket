package com.example.bookwithticket.refund.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookwithticket.refund.dto.RefundRequest;
import com.example.bookwithticket.refund.dto.RefundResponse;
import com.example.bookwithticket.refund.service.RefundService;

@RestController
public class RefundController {
	private final RefundService refundService;
	
	public RefundController(RefundService refundService) {
		this.refundService = refundService;
	}
	
	private Long getCurrentMemberId() {
        return 1L;
    }
	
	@PostMapping("api/payments/{orderNumber}/refund")
	public ResponseEntity<RefundResponse>requestRefund(
			@PathVariable(name = "orderNumber")
			String orderNumber,
			@RequestBody
			RefundRequest request
	){
		Long memberId = getCurrentMemberId();
		
		RefundResponse response = refundService.requestBookRefund(memberId, orderNumber, request.getReason());
		
		return ResponseEntity.ok(response);
	}
}
