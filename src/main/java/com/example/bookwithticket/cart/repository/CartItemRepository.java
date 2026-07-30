package com.example.bookwithticket.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookwithticket.cart.entity.CartItemEntity;

public interface CartItemRepository
        extends JpaRepository<CartItemEntity, Long> {

    List<CartItemEntity> findByCartId(Long cartId);

    Optional<CartItemEntity> findByCartIdAndBookId(Long cartId, Long bookId);

    void deleteByCartId(Long cartId);
    
    List<CartItemEntity> findByIdInAndCartMemberId(List<Long> cartItemIds, Long memberId);
}