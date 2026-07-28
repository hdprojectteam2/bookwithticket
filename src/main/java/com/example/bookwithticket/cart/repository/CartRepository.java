package com.example.bookwithticket.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookwithticket.cart.entity.CartEntity;


public interface CartRepository
        extends JpaRepository<CartEntity, Long> {

    Optional<CartEntity> findByMemberId(Long memberId);
}