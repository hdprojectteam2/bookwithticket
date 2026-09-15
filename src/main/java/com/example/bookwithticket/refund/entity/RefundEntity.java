package com.example.bookwithticket.refund.entity;

import java.time.LocalDateTime;

import com.example.bookwithticket.payment.entity.PaymentEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "refund")
public class RefundEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "refund_id")
	private Long id;

	@Column(name = "requester_id", nullable = false)
	private Long requesterId;

	@Column(name = "approver_id")
	private Long approverId;

	@Column(name = "amount", nullable = false)
	private int amount;

	@Column(name = "reason", nullable = false, length = 255)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(name = "return_method")
	private ReturnMethod returnMethod;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false, unique = true)
	private PaymentEntity payment;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private RefundStatus status;

	@Column(name = "requested_at", nullable = false)
	private LocalDateTime requestedAt;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected RefundEntity() {
	}

	public RefundEntity(Long requesterId, PaymentEntity payment, int amount, String reason) {
		this.requesterId = requesterId;
		this.payment = payment;
		this.amount = amount;
		this.reason = reason;
		this.status = RefundStatus.REQUESTED;
		this.requestedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Long getRequesterId() {
		return requesterId;
	}

	public Long getApproverId() {
		return approverId;
	}

	public int getAmount() {
		return amount;
	}

	public String getReason() {
		return reason;
	}

	public ReturnMethod getReturnMethod() {
		return returnMethod;
	}

	public PaymentEntity getPayment() {
		return payment;
	}

	public RefundStatus getStatus() {
		return status;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
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

	public void approve(Long approverId) {
		if (this.status != RefundStatus.REQUESTED && this.status != RefundStatus.REJECTED) {
			throw new IllegalStateException("환불 요청 상태에서만 승인할 수 있습니다.");
		}
		this.approverId = approverId;
		this.status = RefundStatus.APPROVED;
		this.approvedAt = LocalDateTime.now();
	}

	public void reject(Long approverId) {
		if (this.status != RefundStatus.REQUESTED) {
			throw new IllegalStateException("환불 요청 상태에서만 거절할 수 있습니다.");
		}
		this.approverId = approverId;
		this.status = RefundStatus.REJECTED;
		this.approvedAt = LocalDateTime.now();
	}

	public void complete() {
		if (this.status != RefundStatus.REQUESTED && this.status != RefundStatus.APPROVED) {
			throw new IllegalStateException("완료할 수 없는 상태입니다.");
		}
		this.status = RefundStatus.COMPLETED;
		this.completedAt = LocalDateTime.now();
	}

	public void fail() {
		this.status = RefundStatus.FAILED;
	}

	public void setReturnMethod(ReturnMethod returnMethod) {
		this.returnMethod = returnMethod;
	}

}
