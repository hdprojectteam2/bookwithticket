package com.example.bookwithticket.refund.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.book.repository.BookStockRepository;
import com.example.bookwithticket.domain.reservation.Reservation;
import com.example.bookwithticket.domain.reservation.ReservationRepository;
import com.example.bookwithticket.domain.reservation.ReservationService;
import com.example.bookwithticket.order.entity.BookOrderEntity;
import com.example.bookwithticket.order.entity.BookOrderItemEntity;
import com.example.bookwithticket.order.entity.OrderStatus;
import com.example.bookwithticket.order.repository.BookOrderRepository;
import com.example.bookwithticket.payment.dto.TossPaymentClient;
import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.entity.PaymentStatus;
import com.example.bookwithticket.payment.repository.PaymentRepository;
import com.example.bookwithticket.refund.dto.RefundResponse;
import com.example.bookwithticket.refund.entity.RefundEntity;
import com.example.bookwithticket.refund.entity.RefundStatus;
import com.example.bookwithticket.refund.repository.RefundRepository;
import com.example.bookwithticket.domain.reservation.ReservationStatus;

@Service
@Transactional
public class RefundServiceImpl implements RefundService {

	private final BookOrderRepository bookOrderRepository;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final BookStockRepository bookRepository;
	private final TossPaymentClient tossPaymentClient;
	private final ReservationRepository reservationRepository;
	private final ReservationService reservationService;

	public RefundServiceImpl(BookOrderRepository bookOrderRepository, PaymentRepository paymentRepository,
			RefundRepository refundRepository, BookStockRepository bookRepository, TossPaymentClient tossPaymentClient,
			ReservationRepository reservationRepository, ReservationService reservationService) {
		this.bookOrderRepository = bookOrderRepository;
		this.paymentRepository = paymentRepository;
		this.refundRepository = refundRepository;
		this.bookRepository = bookRepository;
		this.tossPaymentClient = tossPaymentClient;
		this.reservationRepository = reservationRepository;
		this.reservationService = reservationService;
	}

	@Override
	public RefundResponse requestBookRefund(Long memberId, String orderNumber, String reason) {
		validateReason(reason);
		BookOrderEntity order = bookOrderRepository
				.findByOrderNumberAndMemberIdAndOrderStatus(orderNumber, memberId, OrderStatus.PAID)
				.orElseThrow(() -> new IllegalArgumentException("환불할 수 있는 주문이 없습니다."));

		PaymentEntity payment = paymentRepository.findByBookOrderIdAndStatus(order.getId(), PaymentStatus.DONE)
				.orElseThrow(() -> new IllegalArgumentException("환불이 가능한 주문이 없습니다."));

		if (refundRepository.existsByPaymentId(payment.getId())) {
			throw new IllegalArgumentException("이미 환불 요청된 주문입니다.");
		}

		RefundEntity refund = new RefundEntity(memberId, payment, payment.getAmount(), reason);
		refundRepository.save(refund);

		/* 배송 준비 중이면 즉시 환불 */
		if (order.isBeforeShipping()) {
			CompleteRefund(order, payment, refund);
			return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불이 완료되었습니다.");
		}

		/* 배송중이거나 배송 완료될 경우 */
		return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불이 접수되었습니다.");
	}

	@Override
	public RefundResponse approveBookRefund(Long adminId, Long refundId) {
		RefundEntity refund = refundRepository.findByIdAndStatus(refundId, RefundStatus.REQUESTED)
				.orElseThrow(() -> new IllegalArgumentException("환불 요청이 없습니다."));

		PaymentEntity payment = refund.getPayment();
		BookOrderEntity order = payment.getBookOrder();

		if (order == null) {
			throw new IllegalArgumentException("도서 주문 결제가 아닙니다.");
		}

		refund.approve(adminId);
		CompleteRefund(order, payment, refund);
		return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불 승인이 되었습니다.");
	}

	@Override
	public RefundResponse rejectBookRefund(Long adminId, Long refundId) {

		RefundEntity refund = refundRepository.findByIdAndStatus(refundId, RefundStatus.REQUESTED)
				.orElseThrow(() -> new IllegalArgumentException("환불 요청이 없습니다."));

		PaymentEntity payment = refund.getPayment();

		BookOrderEntity order = payment.getBookOrder();

		if (order == null) {
			throw new IllegalArgumentException("도서 주문 결제가 아닙니다.");
		}

		refund.reject(adminId);

		return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불 요청이 거절되었습니다.");
	}

	public void validateReason(String reason) {
		if (reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("환불 사유를 입력해주세요.");
		}
	}

	private void CompleteRefund(BookOrderEntity order, PaymentEntity payment, RefundEntity refund) {
		tossPaymentClient.cancelPayment(payment.getPaymentKey(), refund.getReason());

		payment.cancel();
		order.refund();
		restoreStock(order);
		refund.complete();
	}

	private void restoreStock(BookOrderEntity order) {
		for (BookOrderItemEntity orderItem : order.getOrderItems()) {
			bookRepository.increaseStock(orderItem.getBook().getId(), orderItem.getQuantity());
		}
	}

	@Override
	public RefundResponse requestPerformanceRefund(Long memberId, String reservationNumber, String reason) {

		validateReason(reason);

		Long reservationId;

		try {

			reservationId = Long.parseLong(reservationNumber);

		} catch (NumberFormatException e) {

			throw new IllegalArgumentException("올바르지 않은 예매 ID입니다.");
		}

		Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
				.orElseThrow(() -> new IllegalArgumentException("환불할 수 있는 예매가 없습니다."));

		if (reservation.getStatus() != ReservationStatus.CONFIRMED) {

			throw new IllegalArgumentException("예매 완료 상태만 환불할 수 있습니다.");
		}

		PaymentEntity payment = paymentRepository
				.findFirstByReservationIdAndStatusOrderByCreatedAtDesc(reservation.getId(), PaymentStatus.DONE)
				.orElseThrow(() -> new IllegalArgumentException("환불 가능한 결제 내역이 없습니다."));

		if (refundRepository.existsByPaymentId(payment.getId())) {

			throw new IllegalArgumentException("이미 환불 요청된 예매입니다.");
		}

		RefundEntity refund = new RefundEntity(memberId, payment, payment.getAmount(), reason);

		refundRepository.save(refund);

		tossPaymentClient.cancelPayment(payment.getPaymentKey(), reason);

		payment.cancel();

		reservationService.cancelReservation(memberId, reservation.getId());

		refund.complete();

		return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불이 완료되었습니다.");
	}

	private Long parseReservationId(String reservationNumber) {

		if (reservationNumber == null || !reservationNumber.startsWith("R")) {

			throw new IllegalArgumentException("올바르지 않은 예매번호입니다.");
		}

		try {

			return Long.parseLong(reservationNumber.substring(1));

		} catch (NumberFormatException e) {

			throw new IllegalArgumentException("올바르지 않은 예매번호입니다.");
		}
	}

}
