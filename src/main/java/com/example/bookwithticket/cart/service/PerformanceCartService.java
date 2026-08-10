package com.example.bookwithticket.cart.service;

import java.util.List;

import com.example.bookwithticket.cart.dto.PerformanceCartItemDto;

public interface PerformanceCartService {

    void addCartItem(Long memberId, Long performanceScheduleId);

    List<PerformanceCartItemDto> getCartItems(Long memberId);

    void deleteCartItem(Long memberId, Long cartItemId);
    
    void deleteAllItems(Long memberId);
}