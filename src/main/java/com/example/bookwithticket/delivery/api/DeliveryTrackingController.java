package com.example.bookwithticket.delivery.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeliveryTrackingController {

	private final DeliveryTrackingService deliveryTrackingService;

	public DeliveryTrackingController(DeliveryTrackingService deliveryTrackingService) {
		this.deliveryTrackingService = deliveryTrackingService;
	}

	@GetMapping("/api/delivery/tracking")
	public ResponseEntity<String> getTrackingInfo(@RequestParam String courier, @RequestParam String invoice) {

		String result = deliveryTrackingService.getTrackingInfo(courier, invoice);

		return ResponseEntity.ok().header("Content-Type", "application/json;charset=UTF-8").body(result);
	}

}