package com.example.bookwithticket.cart.service;

import java.util.List;

import com.example.bookwithticket.cart.entity.CartItemEntity;

public interface CartService {

    void addCartItem(Long memberId, Long bookId, String bookTitle, int price, int stock, int quantity);

    List<CartItemEntity> findCartItems(Long memberId);
}