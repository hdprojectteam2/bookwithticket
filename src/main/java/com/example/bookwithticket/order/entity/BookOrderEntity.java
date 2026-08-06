package com.example.bookwithticket.order.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "book_order")
public class BookOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_order_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private AddressEntity address;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    private DeliveryStatus deliveryStatus;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "bookOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BookOrderItemEntity> orderItems = new ArrayList<>();

    protected BookOrderEntity() {
    }

    public BookOrderEntity(
            Long memberId,
            String orderNumber,
            int totalPrice
    ) {
        this.memberId = memberId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;

        this.orderStatus = OrderStatus.PAYMENT_PENDING;
        this.deliveryStatus = DeliveryStatus.READY;
        this.deleted = false;
    }

    public void addOrderItem(BookOrderItemEntity orderItem) {
        this.orderItems.add(orderItem);
    }

    public void updateAddress(AddressEntity address) {
        this.address = address;
    }

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<BookOrderItemEntity> getOrderItems() {
        return orderItems;
    }
    
    public void clearOrderItems() {
        this.orderItems.clear();
    }

    public void updateTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void resetAddress() {
        this.address = null;
    }
    
    public void completePayment() {
        if (this.orderStatus != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("결제 대기 상태의 주문만 결제할 수 있습니다.");
        }

        this.orderStatus = OrderStatus.PAID;
    }
    
    public void cancel() {
        if (this.orderStatus != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("결제 대기 주문만 취소할 수 있습니다.");
        }

        this.orderStatus = OrderStatus.CANCELLED;
    }
    
    public void refund() {
    	if(this.orderStatus != OrderStatus.PAID) {
    		throw new IllegalStateException("결제가 완료된 주문만 환불할 수 있습니다.");
    	}
    	this.orderStatus = OrderStatus.REFUNDED;
    }
    
    public boolean isBeforeShipping() {
    	return this.deliveryStatus == DeliveryStatus.READY;
    }
    
}