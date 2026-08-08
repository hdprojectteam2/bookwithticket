package com.example.bookwithticket.history.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.history.dto.BookOrderHistoryDto;
import com.example.bookwithticket.history.dto.BookOrderHistoryItemDto;
import com.example.bookwithticket.order.entity.BookOrderEntity;
import com.example.bookwithticket.order.entity.BookOrderItemEntity;
import com.example.bookwithticket.order.entity.DeliveryStatus;
import com.example.bookwithticket.order.entity.OrderStatus;
import com.example.bookwithticket.order.repository.BookOrderRepository;
import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.entity.PaymentStatus;
import com.example.bookwithticket.payment.repository.PaymentRepository;
import com.example.bookwithticket.refund.entity.RefundEntity;
import com.example.bookwithticket.refund.entity.RefundStatus;
import com.example.bookwithticket.refund.repository.RefundRepository;


@Service
@Transactional(readOnly = true)
public class BookOrderHistoryServiceImpl implements BookOrderHistoryService {

    private final BookOrderRepository bookOrderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    
    public BookOrderHistoryServiceImpl(BookOrderRepository bookOrderRepository, PaymentRepository paymentRepository, RefundRepository refundRepository) {
    	this.bookOrderRepository = bookOrderRepository;
    	this.paymentRepository = paymentRepository;
    	this.refundRepository = refundRepository;
    }
	
	@Override
	public List<BookOrderHistoryDto> findOrderHistory(Long memberId) {
		List<OrderStatus> visibleStatuses =List.of(OrderStatus.PAID, OrderStatus.REFUNDED);
		
		List<BookOrderEntity> orders = bookOrderRepository.findByMemberIdAndOrderStatusInAndDeletedFalseOrderByCreatedAtDesc(memberId, visibleStatuses);
		
		return orders.stream().map(this::toDto).toList();
	}
	
	private BookOrderHistoryDto toDto(BookOrderEntity order) {
		if(order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
			throw new IllegalStateException("주문 상품 정보가 존재하지 않습니다.");
		}
		
		List<BookOrderHistoryItemDto> itemDtos = order.getOrderItems().stream().map(this::toItemDto).toList();	
		
		String refundStatus = null;
	    String refundStatusCode = null;

	    Optional<PaymentEntity> paymentOptional =
	            paymentRepository.findFirstByBookOrderIdAndStatusInOrderByCreatedAtDesc(order.getId(), List.of(PaymentStatus.DONE, PaymentStatus.CANCELED));

	    if (paymentOptional.isPresent()) {

	        PaymentEntity payment = paymentOptional.get();

	        Optional<RefundEntity> refundOptional = refundRepository.findByPaymentId(payment.getId());

	        if (refundOptional.isPresent()) {

	            RefundEntity refund = refundOptional.get();

	            refundStatus = convertRefundStatus(refund.getStatus());

	            refundStatusCode = refund.getStatus().name();
	        }
	    }
		
		 return new BookOrderHistoryDto(
		            order.getOrderNumber(),
		            order.getCreatedAt(),
		            convertDeliveryStatus(order.getDeliveryStatus()),
		            order.getDeliveryStatus().name(),
		            order.getOrderStatus().name(),
		            refundStatus,
		            refundStatusCode,
		            order.getTotalPrice(),
		            itemDtos
		    );
	}
	
	private BookOrderHistoryItemDto toItemDto(BookOrderItemEntity orderItem) {
        Book book = orderItem.getBook();

        return new BookOrderHistoryItemDto(
                orderItem.getBookTitleSnapshot(),
                book.getAuthor(),
                book.getPublisher(),
                book.getThumbnail(),
                orderItem.getPriceSnapshot(),
                orderItem.getQuantity(),
                orderItem.getTotalPrice()
        );
    }
	
	private String convertRefundStatus(RefundStatus status) {
	    return switch (status) {
	        case REQUESTED -> "환불 요청 중";
	        case APPROVED -> "환불 처리 중";
	        case REJECTED -> "환불 거절";
	        case COMPLETED -> "환불 완료";
	        case FAILED -> "환불 처리 실패";
	    };
	}
	
	private String convertDeliveryStatus(DeliveryStatus status) {
		return switch (status) {
		case READY -> "배송 준비 중";
        case SHIPPING -> "배송 중";
        case DELIVERED -> "배송 완료";
		default -> "배송 확인 중";
		};
	}

}
