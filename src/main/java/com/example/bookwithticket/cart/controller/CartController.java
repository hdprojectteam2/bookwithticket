package com.example.bookwithticket.cart.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bookwithticket.cart.dto.CartItemDto;
import com.example.bookwithticket.cart.service.CartService;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 임시 ID 1
    private Long getCurrentMemberId() {
        return 1L;
    }

    /*장바구니 이동 */
    @GetMapping("/cart")
    public String cartPage(Model model) {

        Long memberId = getCurrentMemberId();

        List<CartItemDto> cartItems = cartService.findCartItems(memberId);

        int totalPrice = cartItems.stream()
                .mapToInt(CartItemDto::getTotalPrice)
                .sum();
        
        int totalQuantity = cartItems.stream()
        		.mapToInt(CartItemDto::getQuantity)
        		.sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalQuantity", totalQuantity);

        return "cart/cartList";
    }

    /*장바구니 목록 조회 */
    @ResponseBody
    @GetMapping("/api/cart")
    public ResponseEntity<List<CartItemDto>> findCartItems() {

        Long memberId = getCurrentMemberId();

        List<CartItemDto> cartItems = cartService.findCartItems(memberId);

        return ResponseEntity.ok(cartItems);
    }

    /*장바구니 상품 추가 */
    @ResponseBody
    @PostMapping("/api/cart/items")
    public ResponseEntity<String> addCartItem(
            @RequestParam(
                    name = "memberId",
                    defaultValue = "1"
            ) Long memberId,
            @RequestParam(name = "bookId") Long bookId,
            @RequestParam(
                    name = "quantity",
                    defaultValue = "1"
            ) int quantity
    ) {
        cartService.addCartItem(
                memberId,
                bookId,
                quantity
        );

        return ResponseEntity.ok(
                "장바구니 등록 완료"
        );
    }
    
    /*장바구니 상품 삭제 */
    @ResponseBody
    @DeleteMapping("/api/cart/items/{cartItemId}")
    public ResponseEntity<String> deleteCartItem(
    		@PathVariable(name = "cartItemId") Long cartItemId
    		){
    	Long memberId = getCurrentMemberId();
    	
    	cartService.deleteCartItem(memberId, cartItemId);
    	
    	return ResponseEntity.ok("장바구니 상품 삭제 완료");
    }
    
    /*장바구니 수량 변경 */
    @ResponseBody
    @PatchMapping("/api/cart/items/{cartItemId}")
    public ResponseEntity<String> updateQuantity(
    		@PathVariable(name = "cartItemId") Long cartItemId,
    		@RequestParam(name = "quantity") int quantity
    		){
    	Long memberId = getCurrentMemberId();
    	
    	cartService.updateQuantity(memberId, cartItemId, quantity);
    	return ResponseEntity.ok("장바구니 상품 수량 변경 완료");
    }

   
}