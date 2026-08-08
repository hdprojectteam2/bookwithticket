package com.example.bookwithticket.cart.dto;

import java.time.LocalDateTime;

import com.example.bookwithticket.cart.entity.PerformanceCartItemEntity;
import com.example.bookwithticket.cart.entity.PerformanceCartStatus;
import com.example.bookwithticket.performance.entity.PerformanceEntity;
import com.example.bookwithticket.performance.entity.PerformanceScheduleEntity;

public class PerformanceCartItemDto {

    private Long cartItemId;

    private Long performanceId;

    private Long scheduleId;

    private String title;

    private LocalDateTime performanceStartAt;

    private LocalDateTime reservationStartAt;

    private LocalDateTime reservationEndAt;

    private PerformanceCartStatus status;

    private boolean clickable;
    
    private String posterUrl;
    
    private String venue;

    public PerformanceCartItemDto(PerformanceCartItemEntity cartItem) {
        PerformanceScheduleEntity schedule = cartItem.getPerformanceSchedule();

        PerformanceEntity performance = schedule.getPerformance();

        this.cartItemId = cartItem.getId();

        this.performanceId = performance.getId();

        this.scheduleId = schedule.getId();

        this.title = performance.getTitle();

        this.performanceStartAt = schedule.getStartAt();

        this.reservationStartAt = schedule.getReservationStartAt();

        this.reservationEndAt = schedule.getReservationEndAt();

        this.posterUrl = performance.getPosterUrl();
        
        this.venue = performance.getVenue();
        
        calculateStatus();
    }

    private void calculateStatus() {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(reservationStartAt)) {
            this.status = PerformanceCartStatus.OPEN_SCHEDULED;

            this.clickable = false;
            return;
        }

        if (!now.isBefore(reservationEndAt)) {
            this.status = PerformanceCartStatus.EXPIRED;

            this.clickable = false;
            return;
        }

        this.status = PerformanceCartStatus.AVAILABLE;

        this.clickable = true;
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

    public LocalDateTime getPerformanceStartAt() {
        return performanceStartAt;
    }

    public LocalDateTime getReservationStartAt() {
        return reservationStartAt;
    }

    public LocalDateTime getReservationEndAt() {
        return reservationEndAt;
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

    public boolean isClickable() {
        return clickable;
    }
    
}