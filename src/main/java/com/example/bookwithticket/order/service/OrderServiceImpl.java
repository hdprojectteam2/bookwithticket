package com.example.bookwithticket.order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.repository.BookStockRepository;
import com.example.bookwithticket.cart.entity.CartItemEntity;
import com.example.bookwithticket.cart.repository.CartItemRepository;
import com.example.bookwithticket.order.dto.AdminOrderItemResponse;
import com.example.bookwithticket.order.dto.AdminOrderResponse;
import com.example.bookwithticket.order.dto.DeliveryStatusRequest;
import com.example.bookwithticket.order.dto.OrderCreateRequest;
import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPreparedRequest;
import com.example.bookwithticket.order.dto.OrderPreparedResponse;
import com.example.bookwithticket.order.dto.OrderPreviewResponse;
import com.example.bookwithticket.order.dto.ShippingRequest;
import com.example.bookwithticket.order.entity.AddressEntity;
import com.example.bookwithticket.order.entity.BookOrderEntity;
import com.example.bookwithticket.order.entity.BookOrderItemEntity;
import com.example.bookwithticket.order.entity.DeliveryStatus;
import com.example.bookwithticket.order.entity.OrderStatus;
import com.example.bookwithticket.order.repository.AddressRepository;
import com.example.bookwithticket.order.repository.BookOrderRepository;
import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.entity.PaymentStatus;
import com.example.bookwithticket.payment.repository.PaymentRepository;
import com.example.bookwithticket.payment.service.PaymentFailureService;
import com.example.bookwithticket.refund.entity.RefundEntity;
import com.example.bookwithticket.refund.repository.RefundRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	private final AddressRepository addressRepository;
	private final CartItemRepository cartItemRepository;
	private final BookOrderRepository bookOrderRepository;
	private final BookStockRepository bookRepository;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final PaymentFailureService paymentFailureService;

	public OrderServiceImpl(CartItemRepository cartItemRepository, BookOrderRepository bookOrderRepository,
			AddressRepository addressRepository, BookStockRepository bookRepository,
			PaymentRepository paymentRepository, RefundRepository refundRepository,
			PaymentFailureService paymentFailureService) {
		this.cartItemRepository = cartItemRepository;
		this.bookOrderRepository = bookOrderRepository;
		this.addressRepository = addressRepository;
		this.bookRepository = bookRepository;
		this.paymentRepository = paymentRepository;
		this.refundRepository = refundRepository;
		this.paymentFailureService = paymentFailureService;

	}

	@Override
	@Transactional(readOnly = true)
	public OrderPageDto findPendingOrder(Long memberId, String orderNumber) {
		if (orderNumber == null || orderNumber.isBlank()) {
			throw new IllegalArgumentException("주문번호가 없습니다.");
		}

		BookOrderEntity order = bookOrderRepository
				.findByOrderNumberAndMemberIdAndOrderStatus(orderNumber, memberId, OrderStatus.PAYMENT_PENDING)
				.orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 주문입니다."));

		return new OrderPageDto(order);
	}

	private void validateRequest(OrderPreparedRequest request) {
		if (request == null || request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
			throw new IllegalArgumentException("주문할 상품을 선택해주세요.");
		}

		if (request.getCartItemIds().contains(null)) {
			throw new IllegalArgumentException("잘못된 장바구니 상품 정보가 포함되어 있습니다.");
		}
	}

	private void validateBook(Book book, int quantity) {
		if (book.getStock() <= 0) {
			throw new IllegalArgumentException(book.getTitle() + " 도서는 품절되었습니다.");
		}

		if (quantity < 1) {
			throw new IllegalArgumentException(book.getTitle() + " 도서의 주문 수량이 올바르지 않습니다.");
		}

		if (quantity > book.getStock()) {
			throw new IllegalArgumentException(book.getTitle() + " 도서의 재고가 부족합니다. 현재 재고는 " + book.getStock() + "개입니다.");
		}

	}

	private String createOrderNumber() {
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		String randomValue = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

		return "B" + date + randomValue;
	}


	@Transactional
	@Override
	public void cancelExpiredOrders() {

		LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);

		List<BookOrderEntity> expiredOrders = bookOrderRepository
				.findByOrderStatusAndCreatedAtBefore(OrderStatus.PAYMENT_PENDING, expirationTime);

		for (BookOrderEntity order : expiredOrders) {

			paymentFailureService.saveExpired(order);

			for (BookOrderItemEntity orderItem : order.getOrderItems()) {

				bookRepository.increaseStock(orderItem.getBook().getId(), orderItem.getQuantity());
			}

			order.cancel();
		}
	}

	@Transactional
	@Override
	public void cancelOrder(Long memberId, String orderNumber) {
		BookOrderEntity order = bookOrderRepository
				.findByOrderNumberAndMemberIdAndOrderStatus(orderNumber, memberId, OrderStatus.PAYMENT_PENDING)
				.orElseThrow(() -> new IllegalArgumentException("취소할 수 있는 주문이 없습니다."));

		for (BookOrderItemEntity orderItem : order.getOrderItems()) {
			bookRepository.increaseStock(orderItem.getBook().getId(), orderItem.getQuantity());
		}

		order.cancel();
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdminOrderResponse> findAdminOrders() {
		List<BookOrderEntity> orders = bookOrderRepository.findAllByOrderByCreatedAtDesc();
		List<PaymentStatus> paymentStatuses = List.of(PaymentStatus.DONE, PaymentStatus.CANCELED);

		return orders.stream()

				.filter(order -> order.getOrderStatus() == OrderStatus.PAID
						|| order.getOrderStatus() == OrderStatus.REFUNDED)

				.map(order -> {
					Long refundId = null;
					String refundStatus = null;
					String refundReason = null;
					String returnMethod = null;

					PaymentEntity payment = paymentRepository
							.findFirstByBookOrderIdAndStatusInOrderByCreatedAtDesc(order.getId(), paymentStatuses)
							.orElse(null);

					if (payment != null) {
						RefundEntity refund = refundRepository.findByPaymentId(payment.getId()).orElse(null);

						if (refund != null) {
							refundId = refund.getId();
							refundStatus = refund.getStatus().name();
							refundReason = refund.getReason();
							returnMethod = refund.getReturnMethod() != null ? refund.getReturnMethod().name() : null;
						}
					}

					AddressEntity addressEntity = order.getAddress();
					String receiverName = null;
					String phone = null;
					String zipCode = null;
					String address = null;
					String detailAddress = null;
					String deliveryRequest = null;

					if (addressEntity != null) {
						receiverName = addressEntity.getRecipient();
						phone = addressEntity.getPhone();
						zipCode = addressEntity.getZipCode();
						address = addressEntity.getAddress();
						detailAddress = addressEntity.getDetailAddress();
						deliveryRequest = addressEntity.getDeliveryRequest();
					}

					List<AdminOrderItemResponse> items = order.getOrderItems().stream()

							.map(orderItem -> new AdminOrderItemResponse(orderItem.getId(),
									orderItem.getBookTitleSnapshot(), orderItem.getPriceSnapshot(),
									orderItem.getQuantity(), orderItem.getTotalPrice()))
							.toList();

					return new AdminOrderResponse(order.getId(), order.getMemberId(), order.getOrderNumber(),
							order.getCreatedAt(), order.getTotalPrice(), order.getOrderStatus().name(),
							order.getDeliveryStatus().name(), receiverName, phone, zipCode, address, detailAddress,
							deliveryRequest, order.getCourier(), order.getTrackingNumber(), items, refundId,
							refundStatus, refundReason, returnMethod);
				})

				.toList();
	}

	@Override
	public void updateTrackingInfo(String orderNumber, ShippingRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("배송 정보가 없습니다.");
		}

		BookOrderEntity order = bookOrderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		order.updateTrackingInfo(request.getCourier(), request.getTrackingNumber());
	}

	@Override
	public void updateDeliveryStatus(String orderNumber, DeliveryStatusRequest request) {
		if (request == null || request.getDeliveryStatus() == null || request.getDeliveryStatus().isBlank()) {
			throw new IllegalArgumentException("배송 상태를 선택해주세요.");
		}

		BookOrderEntity order = bookOrderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
		DeliveryStatus deliveryStatus;

		try {
			deliveryStatus = DeliveryStatus.valueOf(request.getDeliveryStatus().trim().toUpperCase());

		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("올바르지 않은 배송 상태입니다.");
		}

		order.updateDeliveryStatus(deliveryStatus);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderPageDto findCompletedOrder(Long memberId, String orderNumber) {
		if (orderNumber == null || orderNumber.isBlank()) {
			throw new IllegalArgumentException("주문번호가 없습니다.");
		}

		BookOrderEntity order = bookOrderRepository.findByOrderNumberAndMemberId(orderNumber, memberId)
				.orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 주문입니다."));

		if (order.getOrderStatus() != OrderStatus.PAID && order.getOrderStatus() != OrderStatus.REFUNDED) {
			throw new IllegalArgumentException("결제가 완료된 주문이 아닙니다.");
		}

		return new OrderPageDto(order);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderPreviewResponse previewOrder(Long memberId, OrderPreparedRequest request) {
		validateRequest(request);
		List<Long> cartItemIds = request.getCartItemIds();
		Set<Long> uniqueCartItemIds = new HashSet<>(cartItemIds);

		if (uniqueCartItemIds.size() != cartItemIds.size()) {
			throw new IllegalArgumentException("중복된 장바구니 상품이 포함되어 있습니다.");
		}

		List<CartItemEntity> cartItems = cartItemRepository.findByIdInAndCartMemberId(cartItemIds, memberId);

		if (cartItems.size() != cartItemIds.size()) {
			throw new IllegalArgumentException("주문할 수 없는 장바구니 상품이 포함되어 있습니다.");
		}

		int totalQuantity = 0;
		int originalPrice = 0;
		int totalPrice = 0;

		for (CartItemEntity cartItem : cartItems) {
			Book book = cartItem.getBook();
			int quantity = cartItem.getQuantity();
			validateBook(book, quantity);
			totalQuantity += quantity;
			originalPrice += book.getPrice() * quantity;
			totalPrice += book.getSalePrice() * quantity;
		}

		return new OrderPreviewResponse(totalQuantity, originalPrice, totalPrice);
	}

	@Override
	@Transactional
	public OrderPreparedResponse createOrder(Long memberId, OrderCreateRequest request) {

		if (request == null || request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {

			throw new IllegalArgumentException("주문할 상품을 선택해주세요.");
		}

		List<Long> cartItemIds = request.getCartItemIds();

		Set<Long> uniqueCartItemIds = new HashSet<>(cartItemIds);

		if (uniqueCartItemIds.size() != cartItemIds.size()) {
			throw new IllegalArgumentException("중복된 장바구니 상품이 포함되어 있습니다.");
		}

		List<CartItemEntity> cartItems = cartItemRepository.findByIdInAndCartMemberId(cartItemIds, memberId);

		if (cartItems.size() != cartItemIds.size()) {
			throw new IllegalArgumentException("주문할 수 없는 장바구니 상품이 포함되어 있습니다.");
		}

		if (request.getRecipient() == null || request.getRecipient().isBlank()) {
			throw new IllegalArgumentException("받는 분을 입력해주세요.");
		}

		if (request.getPhone() == null || request.getPhone().isBlank()) {
			throw new IllegalArgumentException("연락처를 입력해주세요.");
		}

		if (request.getZipcode() == null || request.getZipcode().isBlank()) {
			throw new IllegalArgumentException("우편번호를 입력해주세요.");
		}

		if (request.getAddress() == null || request.getAddress().isBlank()) {
			throw new IllegalArgumentException("주소를 입력해주세요.");
		}

		int calculatedTotalPrice = 0;

		for (CartItemEntity cartItem : cartItems) {
			Book book = cartItem.getBook();
			int quantity = cartItem.getQuantity();
			validateBook(book, quantity);
			calculatedTotalPrice += book.getSalePrice() * quantity;
		}

		for (CartItemEntity cartItem : cartItems) {
			Book book = cartItem.getBook();
			int quantity = cartItem.getQuantity();
			int updatedCount = bookRepository.decreaseStock(book.getId(), quantity);
			if (updatedCount == 0) {
				throw new IllegalStateException(book.getTitle() + " 도서의 재고가 부족합니다.");
			}
		}

		BookOrderEntity order = new BookOrderEntity(memberId, createOrderNumber(), calculatedTotalPrice);

		for (CartItemEntity cartItem : cartItems) {
			BookOrderItemEntity orderItem = new BookOrderItemEntity(order, cartItem.getBook(), cartItem.getQuantity());
			order.addOrderItem(orderItem);
		}

		String deliveryRequest = request.getDeliveryRequest();

		if (deliveryRequest == null || deliveryRequest.trim().isEmpty()) {
			deliveryRequest = null;
		} else {

			deliveryRequest = deliveryRequest.trim();
		}

		AddressEntity address = new AddressEntity(memberId, request.getRecipient(), request.getPhone(),
				request.getZipcode(), request.getAddress(), request.getDetailAddress(), deliveryRequest);

		AddressEntity savedAddress = addressRepository.save(address);

		order.updateAddress(savedAddress);

		BookOrderEntity savedOrder = bookOrderRepository.save(order);

		return new OrderPreparedResponse(savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getTotalPrice(),
				savedOrder.getOrderStatus().name());
	}
}