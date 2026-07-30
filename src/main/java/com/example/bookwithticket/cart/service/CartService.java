package com.example.bookwithticket.cart.service;

import java.util.List;

import com.example.bookwithticket.cart.dto.CartItemDto;

public interface CartService {

    void addCartItem(Long memberId, Long bookId, int quantity);

    List<CartItemDto> findCartItems(Long memberId);

    void deleteCartItem(Long memberId, Long cartItemId);

    void updateQuantity(Long memberId, Long cartItemId, int quantity);

    void deleteExpiredCarts();
    
    void deleteAllItems(Long memberId);
}