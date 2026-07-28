package com.example.bookwithticket.cart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.cart.entity.CartEntity;
import com.example.bookwithticket.cart.entity.CartItemEntity;
import com.example.bookwithticket.cart.repository.CartItemRepository;
import com.example.bookwithticket.cart.repository.CartRepository;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private static final int MAX_QUANTITY = 99;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public void addCartItem(Long memberId, Long bookId, String bookTitle, int price, int stock, int quantity) {

        if (stock <= 0) {
            throw new IllegalArgumentException(
                    "품절된 도서입니다."
            );
        }

        if (quantity > stock) {
            throw new IllegalArgumentException(
                    "도서 재고가 부족합니다."
            );
        }

        CartEntity cart = cartRepository
                .findByMemberId(memberId)
                .orElseGet(() ->
                        cartRepository.save(
                                new CartEntity(memberId)
                        )
                );

        CartItemEntity cartItem = cartItemRepository
                .findByCartIdAndBookId(
                        cart.getId(),
                        bookId
                )
                .orElse(null);

        if (cartItem != null) {
            int newQuantity =
                    cartItem.getQuantity() + quantity;


            if (newQuantity > stock) {
                throw new IllegalArgumentException(
                        "재고보다 많이 담을 수 없습니다."
                );
            }

            cartItem.increaseQuantity(quantity);

        } else {
            cartItem = new CartItemEntity(cart, bookId, bookTitle, price, quantity);
        }

        cartItemRepository.save(cartItem);
        cartItem.getCart().updateModifiedTime();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemEntity> findCartItems(
            Long memberId
    ) {
        return cartRepository.findByMemberId(memberId)
                .map(cart ->
                        cartItemRepository.findByCartId(
                                cart.getId()
                        )
                )
                .orElse(List.of());
    }

}