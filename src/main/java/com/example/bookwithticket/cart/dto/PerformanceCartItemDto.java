package com.example.bookwithticket.cart.dto;

import java.time.LocalDateTime;

import com.example.bookwithticket.cart.entity.PerformanceCartItemEntity;
import com.example.bookwithticket.cart.entity.PerformanceCartStatus;
import com.example.bookwithticket.domain.performance.Performance;
import com.example.bookwithticket.domain.performance.PerformanceSchedule;

public class PerformanceCartItemDto {

	private Long cartItemId;

	private Long performanceId;

	private Long scheduleId;

	private String title;

	private LocalDateTime performanceTime;

	private LocalDateTime ticketOpenTime;

	private PerformanceCartStatus status;

	private String posterUrl;

	private String venue;

	public PerformanceCartItemDto(PerformanceCartItemEntity cartItem) {

		PerformanceSchedule schedule = cartItem.getPerformanceSchedule();

		Performance performance = schedule.getPerformance();

		this.cartItemId = cartItem.getId();

		this.performanceId = performance.getId();

		this.scheduleId = schedule.getId();

		this.title = performance.getTitle();

		this.performanceTime = schedule.getPerformanceTime();

		this.ticketOpenTime = schedule.getTicketOpenTime();

		this.posterUrl = performance.getPosterUrl();

		this.venue = performance.getVenue();

		calculateStatus();
	}

	private void calculateStatus() {

		LocalDateTime now = LocalDateTime.now();

		if (now.isBefore(ticketOpenTime)) {

			this.status = PerformanceCartStatus.OPEN_SCHEDULED;

			return;
		}

		if (!now.isBefore(performanceTime)) {

			this.status = PerformanceCartStatus.EXPIRED;

			return;
		}

		this.status = PerformanceCartStatus.AVAILABLE;

	}

	public Long getCartItemId() {
		return cartItemId;
	}

	public Long getPerformanceId() {
		return performanceId;
	}

	public Long getScheduleId() {
		return scheduleId;
	}

	public String getTitle() {
		return title;
	}

	public LocalDateTime getPerformanceTime() {
		return performanceTime;
	}

	public LocalDateTime getTicketOpenTime() {
		return ticketOpenTime;
	}

	public PerformanceCartStatus getStatus() {
		return status;
	}

	public String getPosterUrl() {
		return posterUrl;
	}

	public String getVenue() {
		return venue;
	}
}