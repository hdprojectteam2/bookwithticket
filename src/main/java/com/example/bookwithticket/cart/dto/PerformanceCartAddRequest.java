package com.example.bookwithticket.cart.dto;

public class PerformanceCartAddRequest {

    private Long performanceScheduleId;

    public PerformanceCartAddRequest() {
    }

    public Long getPerformanceScheduleId() {
        return performanceScheduleId;
    }

    public void setPerformanceScheduleId(Long performanceScheduleId) {
        this.performanceScheduleId = performanceScheduleId;
    }
}