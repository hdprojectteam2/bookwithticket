package com.example.bookwithticket.history.dto;

import java.time.LocalDateTime;

public class PerformanceHistoryDto {

	private final String reservationNumber;
	private final String performanceTitle;
	private final LocalDateTime performanceStartAt;
	private final String posterUrl;
	private final String venue;
	private final String seatNumber;
	private final int totalPrice;
	private final String reservationStatus;
	private final String reservationStatusCode;
	private final String refundStatus;
	private final String refundStatusCode;
	private final String paymentMethod;
	private final LocalDateTime paidAt;

	public PerformanceHistoryDto(String reservationNumber, String performanceTitle, LocalDateTime performanceStartAt,
			String posterUrl, String venue, String seatNumber, int totalPrice, String reservationStatus,
			String reservationStatusCode, String refundStatus, String refundStatusCode, String paymentMethod,
			LocalDateTime paidAt) {
		this.reservationNumber = reservationNumber;
		this.performanceTitle = performanceTitle;
		this.performanceStartAt = performanceStartAt;
		this.posterUrl = posterUrl;
		this.venue = venue;
		this.seatNumber = seatNumber;
		this.totalPrice = totalPrice;
		this.reservationStatus = reservationStatus;
		this.reservationStatusCode = reservationStatusCode;
		this.refundStatus = refundStatus;
		this.refundStatusCode = refundStatusCode;
		this.paymentMethod = paymentMethod;
		this.paidAt = paidAt;
	}

	public String getReservationNumber() {
		return reservationNumber;
	}

	public String getPerformanceTitle() {
		return performanceTitle;
	}

	public LocalDateTime getPerformanceStartAt() {
		return performanceStartAt;
	}

	public String getPosterUrl() {
		return posterUrl;
	}

	public String getVenue() {
		return venue;
	}

	public String getSeatNumber() {
		return seatNumber;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public String getReservationStatus() {
		return reservationStatus;
	}

	public String getReservationStatusCode() {
		return reservationStatusCode;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public String getRefundStatusCode() {
		return refundStatusCode;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}
}