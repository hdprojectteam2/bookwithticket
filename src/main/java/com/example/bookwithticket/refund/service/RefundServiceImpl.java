package com.example.bookwithticket.refund.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.book.repository.BookRepository;
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

@Service
@Transactional
public class RefundServiceImpl implements RefundService {

	private final BookOrderRepository bookOrderRepository;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final BookRepository bookRepository;
	private final TossPaymentClient tossPaymentClient;
	
	
	public RefundServiceImpl(BookOrderRepository bookOrderRepository, PaymentRepository paymentRepository,
			RefundRepository refundRepository, BookRepository bookRepository, TossPaymentClient tossPaymentClient) {
		this.bookOrderRepository = bookOrderRepository;
		this.paymentRepository = paymentRepository;
		this.refundRepository = refundRepository;
		this.bookRepository = bookRepository;
		this.tossPaymentClient = tossPaymentClient;
	}

	@Override
	public RefundResponse requestBookRefund(Long memberId, String orderNumber, String reason) {
		validateReason(reason);
		BookOrderEntity order = bookOrderRepository.findByOrderNumberAndMemberIdAndOrderStatus(orderNumber, memberId, OrderStatus.PAID)
				.orElseThrow(()-> 
						new IllegalArgumentException("환불할 수 있는 주문이 없습니다."));
		
		PaymentEntity payment = paymentRepository.findByBookOrderIdAndStatus(order.getId(), PaymentStatus.DONE)
				.orElseThrow(()->
				new IllegalArgumentException("환불이 가능한 주문이 없습니다."));
			
		if(refundRepository.existsByPaymentId(payment.getId())) {
			throw new IllegalArgumentException("이미 환불 요청된 주문입니다.");
		}
		
		RefundEntity refund = new RefundEntity(memberId, payment, payment.getAmount(), reason);
		refundRepository.save(refund);
		
		/* 배송 준비 중이면 즉시 환불 */
		if(order.isBeforeShipping()) {
			CompleteRefund(order, payment, refund);
			return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불이 완료되었습니다.");
		}
		
		/* 배송중이거나 배송 완료될 경우 */
		return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불이 접수되었습니다.");
	}

	@Override
	public RefundResponse approveBookRefund(Long adminId, Long refundId) {
		RefundEntity refund = refundRepository.findByIdAndStatus(refundId, RefundStatus.REQUESTED)
				.orElseThrow(()->
					new IllegalArgumentException("환불 요청이 없습니다."));
		
		PaymentEntity payment = refund.getPayment();
		BookOrderEntity order = payment.getBookOrder();
		
		if(order == null) {
			throw new IllegalArgumentException("도서 주문 결제가 아닙니다.");
		}
		
		refund.approve(adminId);
		CompleteRefund(order, payment, refund);
		return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불 승인이 되었습니다.");
	}

	@Override
	public RefundResponse rejectBookRefund(Long adminId, Long refundId) {
		RefundEntity refund = refundRepository.findByIdAndStatus(refundId, RefundStatus.REQUESTED)
				.orElseThrow(()->
					new IllegalArgumentException("환불 요청이 없습니다."));
		
		PaymentEntity payment = refund.getPayment();
		BookOrderEntity order = payment.getBookOrder();
		
		if(order == null) {
			throw new IllegalArgumentException("도서 주문 결제가 아닙니다.");
		}
		
		refund.reject(adminId);
		CompleteRefund(order, payment, refund);
		return new RefundResponse(refund.getId(), refund.getStatus().name(), "환불 승인이 되었습니다.");
	}
	
	public void validateReason(String reason) {
		if(reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("환불 사유를 입력해주세요.");
		}
	}
	
	private void CompleteRefund(BookOrderEntity order, PaymentEntity payment, RefundEntity refund) {
		tossPaymentClient.cacelPayment(payment.getPaymentKey(), refund.getReason());
		
		payment.cancle();
		order.refund();
		restoreStock(order);
		refund.complete();
	}
	
	private void restoreStock(BookOrderEntity order) {
		for(BookOrderItemEntity orderItem : order.getOrderItems()) {
			bookRepository.increaseStock(orderItem.getBook().getId(), orderItem.getQuantity());
		}
	}

}
