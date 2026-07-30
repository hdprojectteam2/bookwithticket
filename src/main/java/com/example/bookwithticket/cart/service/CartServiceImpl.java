package com.example.bookwithticket.cart.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.book.entity.BookEntity;
import com.example.bookwithticket.book.repository.BookRepository;
import com.example.bookwithticket.cart.dto.CartItemDto;
import com.example.bookwithticket.cart.entity.CartEntity;
import com.example.bookwithticket.cart.entity.CartItemEntity;
import com.example.bookwithticket.cart.repository.CartItemRepository;
import com.example.bookwithticket.cart.repository.CartRepository;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public void addCartItem(
            Long memberId,
            Long bookId,
            int quantity
    ) {
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "수량은 1개 이상이어야 합니다."
            );
        }

        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "도서를 찾을 수 없습니다."
                        )
                );

	     // 비활성화 도서 검사
	     if (!book.isActive()) {
	         throw new IllegalArgumentException(
	                 "현재 판매하지 않는 도서입니다."
	         );
	     }
	
	     // 삭제된 도서 검사
	     if (book.isDeleted()) {
	         throw new IllegalArgumentException(
	                 "삭제된 도서입니다."
	         );
	     }
	     
	    //품절 도서 검사
        if (book.getStock() <= 0) {
            throw new IllegalArgumentException(
                    "품절된 도서입니다."
            );
        }

        if (quantity > book.getStock()) {
            throw new IllegalArgumentException(
                    "도서 재고가 부족합니다."
            );
        }

        if (quantity > book.getMaxPurchaseQty()) {
            throw new IllegalArgumentException(
                    "최대 구매 수량을 초과했습니다."
            );
        }

        CartEntity cart = cartRepository
                .findByMemberId(memberId)
                .orElseGet(() ->
                        cartRepository.save(
                                new CartEntity(memberId)
                        )
                );

        CartItemEntity cartItem = cartItemRepository
                .findByCartIdAndBookId(
                        cart.getId(),
                        bookId
                )
                .orElse(null);

        if (cartItem != null) {
            int newQuantity =
                    cartItem.getQuantity() + quantity;

            if (newQuantity > book.getStock()) {
                throw new IllegalArgumentException(
                        "재고보다 많이 담을 수 없습니다."
                );
            }

            if (newQuantity > book.getMaxPurchaseQty()) {
                throw new IllegalArgumentException(
                        "최대 구매 수량을 초과했습니다."
                );
            }

            cartItem.increaseQuantity(quantity);

        } else {
            cartItem = new CartItemEntity(
                    cart,
                    book,
                    quantity
            );
        }

        cartItemRepository.save(cartItem);
        cart.updateModifiedTime();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemDto> findCartItems(Long memberId) {

        return cartRepository.findByMemberId(memberId)
                .map(cart ->
                        cartItemRepository
                                .findByCartId(cart.getId())
                                .stream()
                                .map(CartItemDto::new)
                                .toList()
                )
                .orElse(List.of());
    }

	@Override
	public void deleteCartItem(Long memberId, Long cartItemId) {
		CartItemEntity cartItem = findMyCartItem(memberId, cartItemId);
		cartItemRepository.delete(cartItem);
		cartItem.getCart().updateModifiedTime();
	}
	
	@Override
	public void updateQuantity(
	        Long memberId,
	        Long cartItemId,
	        int quantity
	) {
	    CartItemEntity cartItem =
	            findMyCartItem(memberId, cartItemId);

	    BookEntity book = cartItem.getBook();

	    if (book.isDeleted()) {
	        throw new IllegalArgumentException("삭제된 도서입니다.");
	    }

	    if (!book.isActive()) {
	        throw new IllegalArgumentException("판매가 중지된 도서입니다.");
	    }

	    if (!"ON_SALE".equals(book.getSaleStatus())) {
	        throw new IllegalArgumentException("품절된 도서입니다.");
	    }

	    if (book.getStock() <= 0) {
	        throw new IllegalArgumentException("품절된 도서입니다.");
	    }

	    if (quantity < 1) {
	        throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
	    }

	    if (quantity > book.getStock()) {
	        throw new IllegalArgumentException("현재 재고는 " + book.getStock() + "개입니다.");
	    }

	    if (quantity > book.getMaxPurchaseQty()) {
	        throw new IllegalArgumentException("최대 " + book.getMaxPurchaseQty() + "개까지 구매할 수 있습니다.");
	    }

	    cartItem.updateQuantity(quantity);
	    cartItem.getCart().updateModifiedTime();
	}
	
	private CartItemEntity findMyCartItem(Long memberId, Long cartItemId) {
		CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
				.orElseThrow(() ->
						new IllegalArgumentException("장바구니에서 상품을 찾을 수 없습니다"));
		if(!cartItem.getCart()
				.getMemberId()
				.equals(memberId)) {
			throw new IllegalArgumentException("본인 장바구니만 수정 가능합니다.");
		}
		return cartItem;
	}

	@Override
	public void deleteExpiredCarts() {
		LocalDateTime expireDate = LocalDateTime.now().minusDays(30);
		
		List<CartEntity> expiredCarts = cartRepository.findByUpdatedAtBefore(expireDate);
		
		for(CartEntity cart : expiredCarts) {
			cartItemRepository.deleteByCartId(cart.getId());
			cartRepository.delete(cart);
		}
		
	}

	@Override
	public void deleteAllItems(Long memberId) {
		 CartEntity cart = cartRepository.findByMemberId(memberId)
		                    .orElseThrow(() ->
		                            new IllegalArgumentException(
		                                    "장바구니를 찾을 수 없습니다."
		                            )
		                    );

		    cartItemRepository.deleteByCartId(
		            cart.getId()
		    );
	}



	


}