package com.example.bookwithticket.order.dto;

import java.time.LocalDateTime;

public class AdminReservationResponse {

	private final Long reservationId;
	private final String reservationNumber;
	private final Long memberId;
	private final LocalDateTime createdAt;
	private final String performanceTitle;
	private final LocalDateTime performanceTime;
	private final String seatNumber;
	private final int totalPrice;
	private final String reservationStatus;

	public AdminReservationResponse(Long reservationId, String reservationNumber, Long memberId, LocalDateTime createdAt,
			String performanceTitle, LocalDateTime performanceTime, String seatNumber, int totalPrice,
			String reservationStatus) {

		this.reservationId = reservationId;
	    this.reservationNumber = reservationNumber;
		this.memberId = memberId;
		this.createdAt = createdAt;
		this.performanceTitle = performanceTitle;
		this.performanceTime = performanceTime;
		this.seatNumber = seatNumber;
		this.totalPrice = totalPrice;
		this.reservationStatus = reservationStatus;
	}

	public Long getReservationId() {
		return reservationId;
	}
	
	public String getReservationNumber() {
	    return reservationNumber;
	}

	public Long getMemberId() {
		return memberId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
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