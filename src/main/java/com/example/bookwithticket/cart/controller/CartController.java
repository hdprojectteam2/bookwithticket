package com.example.bookwithticket.cart.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bookwithticket.cart.dto.CartItemDto;
import com.example.bookwithticket.cart.dto.PerformanceCartItemDto;
import com.example.bookwithticket.cart.service.CartService;
import com.example.bookwithticket.cart.service.PerformanceCartService;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;

@Controller
public class CartController {

	private final CartService cartService;
	private final PerformanceCartService performanceCartService;
	private final MemberService memberService;

	public CartController(CartService cartService, PerformanceCartService performanceCartService,
			MemberService memberService) {
		this.cartService = cartService;
		this.performanceCartService = performanceCartService;
		this.memberService = memberService;
	}

	private Long getCurrentMemberId(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("로그인이 필요합니다.");
		}

		String email = authentication.getName();

		Member member = memberService.findMyInfo(email);

		return member.getId();
	}

	/* 장바구니 페이지 */
	@GetMapping("/cart")
	public String cartPage(Authentication authentication) {
		
		return "cart/cartList";
	}

	/* 도서 장바구니 조회 */
	@ResponseBody
	@GetMapping("/api/cart")
	public ResponseEntity<List<CartItemDto>> findCartItems(Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		List<CartItemDto> cartItems = cartService.findCartItems(memberId);

		return ResponseEntity.ok(cartItems);
	}

	/* 공연 장바구니 조회 */
	@ResponseBody
	@GetMapping("/api/cart/performances")
	public ResponseEntity<List<PerformanceCartItemDto>> findPerformanceCartItems(Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		List<PerformanceCartItemDto> performanceCartItems = performanceCartService.getCartItems(memberId);

		return ResponseEntity.ok(performanceCartItems);
	}

	/* 도서 장바구니 추가 */
	@ResponseBody
	@PostMapping("/api/cart/items")
	public ResponseEntity<String> addCartItem(@RequestParam(name = "bookId") Long bookId,
			@RequestParam(name = "quantity", defaultValue = "1") int quantity, Authentication authentication) {
		Long memberId = getCurrentMemberId(authentication);

		cartService.addCartItem(memberId, bookId, quantity);

		return ResponseEntity.ok("장바구니 등록 완료");
	}

	/* 도서 장바구니 삭제 */
	@ResponseBody
	@DeleteMapping("/api/cart/items/{cartItemId}")
	public ResponseEntity<String> deleteCartItem(@PathVariable(name = "cartItemId") Long cartItemId,
			Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		cartService.deleteCartItem(memberId, cartItemId);

		return ResponseEntity.ok("장바구니 상품 삭제 완료");
	}

	/* 도서 수량 변경 */
	@ResponseBody
	@PatchMapping("/api/cart/items/{cartItemId}")
	public ResponseEntity<String> updateQuantity(@PathVariable(name = "cartItemId") Long cartItemId,

			@RequestParam(name = "quantity") int quantity, Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		cartService.updateQuantity(memberId, cartItemId, quantity);

		return ResponseEntity.ok("장바구니 상품 수량 변경 완료");
	}

	/* 도서 전체 삭제 */
	@ResponseBody
	@DeleteMapping("/api/cart/items")
	public ResponseEntity<String> deleteAllCartItems(Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		cartService.deleteAllItems(memberId);

		return ResponseEntity.ok("장바구니 상품을 모두 삭제했습니다.");
	}

	/* 공연 장바구니 추가 */
	@ResponseBody
	@PostMapping("/api/cart/performances")
	public ResponseEntity<String> addPerformanceCartItem(
			@RequestParam(name = "performanceScheduleId") Long performanceScheduleId, Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		performanceCartService.addCartItem(memberId, performanceScheduleId);

		return ResponseEntity.ok("공연 장바구니 등록 완료");
	}

	/* 공연 장바구니 삭제 */
	@ResponseBody
	@DeleteMapping("/api/cart/performances/{performanceCartItemId}")
	public ResponseEntity<String> deletePerformanceCartItem(
			@PathVariable(name = "performanceCartItemId") Long performanceCartItemId, Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		performanceCartService.deleteCartItem(memberId, performanceCartItemId);

		return ResponseEntity.ok("공연 장바구니 상품 삭제 완료");
	}

	/* 공연 전체 삭제 */
	@ResponseBody
	@DeleteMapping("/api/cart/performances")
	public ResponseEntity<String> deleteAllPerformanceCartItems(Authentication authentication) {

		Long memberId = getCurrentMemberId(authentication);

		performanceCartService.deleteAllItems(memberId);

		return ResponseEntity.ok("공연 장바구니를 모두 삭제했습니다.");
	}

	/* 장바구니 테스트 */
	@GetMapping("/cartTest")
	public String cartTestPage() {

		return "cartTest";
	}
}