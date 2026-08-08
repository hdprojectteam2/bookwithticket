package com.example.bookwithticket.history.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PerformanceHistoryDto {
	private final String reservationNumber;
	private final String performanceTitle;
	private final LocalDateTime performanceStartAt;
	private final String posterUrl;
	private final String venue;
	private final int totalPrice;
	private final String reservationStatus;
    private final String reservationStatusCode;
    private String refundStatus;
    private String refundStatusCode;
    private final List<String> seatNumbers;
    
	public PerformanceHistoryDto(String reservationNumber, String performanceTitle, LocalDateTime performanceStartAt,
			String posterUrl, String venue, int totalPrice, String reservationStatus, String reservationStatusCode,
			String refundStatus, String refundStatusCode, List<String> seatNumbers) {
		this.reservationNumber = reservationNumber;
		this.performanceTitle = performanceTitle;
		this.performanceStartAt = performanceStartAt;
		this.posterUrl = posterUrl;
		this.venue = venue;
		this.totalPrice = totalPrice;
		this.reservationStatus = reservationStatus;
		this.reservationStatusCode = reservationStatusCode;
		this.refundStatus = refundStatus;
		this.refundStatusCode = refundStatusCode;
		this.seatNumbers = seatNumbers;
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
	
	public List<String> getSeatNumbers() {
	    return seatNumbers;
	}
}