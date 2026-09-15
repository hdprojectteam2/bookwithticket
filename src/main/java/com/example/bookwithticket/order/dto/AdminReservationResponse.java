package com.example.bookwithticket.order.dto;

import java.time.LocalDateTime;

public class AdminReservationResponse {

	private final Long reservationId;
	private final Long memberId;
	private final LocalDateTime reservedAt;
	private final String performanceTitle;
	private final LocalDateTime performanceTime;
	private final String seatNumber;
	private final int totalPrice;
	private final String reservationStatus;

	public AdminReservationResponse(Long reservationId, Long memberId, LocalDateTime reservedAt,
			String performanceTitle, LocalDateTime performanceTime, String seatNumber, int totalPrice,
			String reservationStatus) {

		this.reservationId = reservationId;
		this.memberId = memberId;
		this.reservedAt = reservedAt;
		this.performanceTitle = performanceTitle;
		this.performanceTime = performanceTime;
		this.seatNumber = seatNumber;
		this.totalPrice = totalPrice;
		this.reservationStatus = reservationStatus;
	}

	public Long getReservationId() {
		return reservationId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public LocalDateTime getReservedAt() {
		return reservedAt;
	}

	public String getPerformanceTitle() {
		return performanceTitle;
	}

	public LocalDateTime getPerformanceTime() {
		return performanceTime;
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
}