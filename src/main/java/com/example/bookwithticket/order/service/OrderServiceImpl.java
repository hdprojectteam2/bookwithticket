package com.example.bookwithticket.order.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.book.entity.BookEntity;
import com.example.bookwithticket.cart.entity.CartItemEntity;
import com.example.bookwithticket.cart.repository.CartItemRepository;
import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPreparedRequest;
import com.example.bookwithticket.order.dto.OrderPreparedResponse;
import com.example.bookwithticket.order.entity.BookOrderEntity;
import com.example.bookwithticket.order.entity.BookOrderItemEntity;
import com.example.bookwithticket.order.entity.OrderStatus;
import com.example.bookwithticket.order.repository.BookOrderRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final CartItemRepository cartItemRepository;
    private final BookOrderRepository bookOrderRepository;

    public OrderServiceImpl(
            CartItemRepository cartItemRepository,
            BookOrderRepository bookOrderRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.bookOrderRepository = bookOrderRepository;
    }

    
    @Override
    public OrderPreparedResponse prepareOrder(
            Long memberId,
            OrderPreparedRequest request
    ) {
        validateRequest(request);

        List<Long> cartItemIds = request.getCartItemIds();


        Set<Long> uniqueCartItemIds =
                new HashSet<>(cartItemIds);

        if (uniqueCartItemIds.size()
                != cartItemIds.size()) {
            throw new IllegalArgumentException(
                    "중복된 장바구니 상품이 포함되어 있습니다."
            );
        }

        List<CartItemEntity> cartItems =
                cartItemRepository
                        .findByIdInAndCartMemberId(
                                cartItemIds,
                                memberId
                        );

        
        if (cartItems.size() != cartItemIds.size()) {
            throw new IllegalArgumentException(
                    "주문할 수 없는 장바구니 상품이 포함되어 있습니다."
            );
        }

        
        int calculatedTotalPrice = 0;

        for (CartItemEntity cartItem : cartItems) {
            BookEntity book = cartItem.getBook();
            int quantity = cartItem.getQuantity();

            validateBook(book, quantity);

            calculatedTotalPrice +=
                    book.getSalePrice() * quantity;
        }

        final int totalPrice = calculatedTotalPrice;

        /* PAYMENT_PENDING 주문 상태 유무 조회 */
        BookOrderEntity order =
                bookOrderRepository
                        .findFirstByMemberIdAndOrderStatusOrderByCreatedAtDesc(
                                memberId,
                                OrderStatus.PAYMENT_PENDING
                        )
                        .orElseGet(() ->
                                new BookOrderEntity(
                                        memberId,
                                        createOrderNumber(),
                                        totalPrice
                                )
                        );

        /* 기존 주문 정보 초기화 */
        order.clearOrderItems();
        order.updateTotalPrice(totalPrice);
        order.resetAddress();


        for (CartItemEntity cartItem : cartItems) {
            BookOrderItemEntity orderItem =
                    new BookOrderItemEntity(
                            order,
                            cartItem.getBook(),
                            cartItem.getQuantity()
                    );

            order.addOrderItem(orderItem);
        }

        /* 주문 상품 생성 */
        BookOrderEntity savedOrder =
                bookOrderRepository.save(order);

        return new OrderPreparedResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderStatus().name()
        );
    }

    /* 회원의 PAYMENT_PENDING 상태의 주문 조회 ㄴ*/
    @Override
    @Transactional(readOnly = true)
    public OrderPageDto findPendingOrder(
            Long memberId,
            String orderNumber
    ) {
        if (orderNumber == null
                || orderNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "주문번호가 없습니다."
            );
        }

        BookOrderEntity order =
                bookOrderRepository
                        .findByOrderNumberAndMemberIdAndOrderStatus(
                                orderNumber,
                                memberId,
                                OrderStatus.PAYMENT_PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "조회할 수 없는 주문입니다."
                                )
                        );

        return new OrderPageDto(order);
    }

    /* 주문 요청값 검사 */
    private void validateRequest(
            OrderPreparedRequest request
    ) {
        if (request == null
                || request.getCartItemIds() == null
                || request.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException(
                    "주문할 상품을 선택해주세요."
            );
        }

        if (request.getCartItemIds().contains(null)) {
            throw new IllegalArgumentException(
                    "잘못된 장바구니 상품 정보가 포함되어 있습니다."
            );
        }
    }

    /* 도서 주문 가능 여부 검사 */
    private void validateBook(
            BookEntity book,
            int quantity
    ) {
        if (book.isDeleted()) {
            throw new IllegalArgumentException(
                    book.getTitle() + " 도서는 삭제된 상품입니다."
            );
        }

        if (!book.isActive()) {
            throw new IllegalArgumentException(
                    book.getTitle() + " 도서는 판매가 중지되었습니다."
            );
        }

        if (!"ON_SALE".equals(book.getSaleStatus())) {
            throw new IllegalArgumentException(
                    book.getTitle() + " 도서는 현재 판매 중이 아닙니다."
            );
        }

        if (book.getStock() <= 0) {
            throw new IllegalArgumentException(
                    book.getTitle() + " 도서는 품절되었습니다."
            );
        }

        if (quantity < 1) {
            throw new IllegalArgumentException(
                    book.getTitle() + " 도서의 주문 수량이 올바르지 않습니다."
            );
        }

        if (quantity > book.getStock()) {
            throw new IllegalArgumentException(
                    book.getTitle() + " 도서의 재고가 부족합니다. 현재 재고는 " + book.getStock() + "개입니다."
            );
        }

        if (quantity > book.getMaxPurchaseQty()) {
            throw new IllegalArgumentException(
                    book.getTitle() + " 도서는 최대 " + book.getMaxPurchaseQty() + "권까지 구매할 수 있습니다."
            );
        }
    }

    /* 주문번호 생성 */
    private String createOrderNumber() {
        String date =
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd"
                        )
                );

        String randomValue =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 6)
                        .toUpperCase();

        return "B" + date + randomValue;
    }
}