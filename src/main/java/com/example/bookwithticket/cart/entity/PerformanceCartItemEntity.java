package com.example.bookwithticket.cart.entity;

import java.time.LocalDateTime;

import com.example.bookwithticket.performance.entity.PerformanceScheduleEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "performance_cart_item",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_performance_cart_schedule",
            columnNames = {
                "cart_id",
                "performance_schedule_id"
            }
        )
    }
)

public class PerformanceCartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "cart_id",
        nullable = false
    )
    private CartEntity cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "performance_schedule_id",
        nullable = false
    )
    private PerformanceScheduleEntity performanceSchedule;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PerformanceCartItemEntity() {
    }

    public PerformanceCartItemEntity(CartEntity cart, PerformanceScheduleEntity performanceSchedule) {
        this.cart = cart;
        this.performanceSchedule = performanceSchedule;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public CartEntity getCart() {
        return cart;
    }

    public PerformanceScheduleEntity getPerformanceSchedule() {
        return performanceSchedule;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}