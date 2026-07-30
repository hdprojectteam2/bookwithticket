package com.example.bookwithticket.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookwithticket.order.entity.AddressEntity;

public interface AddressRepository
        extends JpaRepository<AddressEntity, Long> {
}