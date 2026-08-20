package com.example.bookwithticket.payment.entity;

import java.time.LocalDateTime;

import com.example.bookwithticket.order.entity.BookOrderEntity;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment")
public class PaymentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_order_id")
	private BookOrderEntity bookOrder;

	@Column(name = "reservation_id")
	private Long reservationId;

	@Column(name = "payment_key", nullable = false, unique = true, length = 200)
	private String paymentKey;

	@Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
	private String idempotencyKey;

	@Enumerated(EnumType.STRING)
	@Column(name = "method", nullable = false, length = 30)
	private PaymentMethod method;

	@Column(name = "amount", nullable = false)
	private int amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private PaymentStatus status;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "canceled_at")
	private LocalDateTime canceledAt;

	@Column(name = "fail_code", length = 50)
	private String failCode;

	@Column(name = "fail_message", length = 255)
	private String failMessage;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected PaymentEntity() {
	}

	public PaymentEntity(BookOrderEntity bookOrder, String paymentKey, String idempotencyKey, PaymentMethod method,
			int amount, LocalDateTime approvedAt) {
		this.bookOrder = bookOrder;
		this.reservationId = null;
		this.paymentKey = paymentKey;
		this.idempotencyKey = idempotencyKey;
		this.method = method;
		this.amount = amount;
		this.status = PaymentStatus.DONE;
		this.approvedAt = approvedAt;
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

	public BookOrderEntity getBookOrder() {
		return bookOrder;
	}

	public String getPaymentKey() {
		return paymentKey;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public int getAmount() {
		return amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}

	public Long getReservationId() {
		return reservationId;
	}

	public static PaymentEntity failed(BookOrderEntity order, String paymentKey, String idempotencyKey, int amount,
			String failCode, String failMessage) {
		PaymentEntity payment = new PaymentEntity();

		payment.bookOrder = order;
		payment.reservationId = null;
		payment.paymentKey = paymentKey;
		payment.idempotencyKey = idempotencyKey;
		payment.method = PaymentMethod.UNKNOWN;
		payment.amount = amount;
		payment.status = PaymentStatus.FAILED;
		payment.approvedAt = null;
		payment.canceledAt = null;
		payment.failCode = failCode;
		payment.failMessage = failMessage;

		return payment;
	}

	public static PaymentEntity performancePayment(Long reservationId, String paymentKey, String idempotencyKey,
			PaymentMethod method, int amount, LocalDateTime approvedAt) {
		PaymentEntity payment = new PaymentEntity();

		payment.bookOrder = null;
		payment.reservationId = reservationId;
		payment.paymentKey = paymentKey;
		payment.idempotencyKey = idempotencyKey;
		payment.method = method;
		payment.amount = amount;
		payment.status = PaymentStatus.DONE;
		payment.approvedAt = approvedAt;
		payment.canceledAt = null;
		payment.failCode = null;
		payment.failMessage = null;

		return payment;
	}

	public static PaymentEntity failedPerformance(Long reservationId, String paymentKey, String idempotencyKey,
			int amount, String failCode, String failMessage) {
		PaymentEntity payment = new PaymentEntity();

		payment.bookOrder = null;
		payment.reservationId = reservationId;
		payment.paymentKey = paymentKey;
		payment.idempotencyKey = idempotencyKey;
		payment.method = PaymentMethod.UNKNOWN;
		payment.amount = amount;
		payment.status = PaymentStatus.FAILED;
		payment.approvedAt = null;
		payment.canceledAt = null;
		payment.failCode = failCode;
		payment.failMessage = failMessage;

		return payment;
	}

	public void cancel() {
		if (this.status != PaymentStatus.DONE) {
			throw new IllegalStateException("완료된 주문만 취소할 수 있습니다.");
		}
		this.status = PaymentStatus.CANCELED;
		this.canceledAt = LocalDateTime.now();
	}

	public static PaymentEntity expired(BookOrderEntity order, int amount) {

		PaymentEntity payment = new PaymentEntity();

		payment.bookOrder = order;
		payment.amount = amount;
		payment.status = PaymentStatus.EXPIRED;

		payment.failCode = "PAYMENT_EXPIRED";
		payment.failMessage = "결제 제한 시간이 만료되었습니다.";

		return payment;
	}

}