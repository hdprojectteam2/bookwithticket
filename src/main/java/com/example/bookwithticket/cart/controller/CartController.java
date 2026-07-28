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

import com.example.bookwithticket.cart.entity.CartItemEntity;
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

    //장바구니 이동
    @GetMapping("/cart")
    public String cartPage(Model model) {

        Long memberId = getCurrentMemberId();

        List<CartItemEntity> cartItems =
                cartService.findCartItems(memberId);

        int totalPrice = cartItems.stream()
                .mapToInt(CartItemEntity::getTotalPrice)
                .sum();
        
        int totalQuantity = cartItems.stream()
        		.mapToInt(CartItemEntity::getQuantity)
        		.sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalQuantity", totalQuantity);

        return "cart/cartList";
    }

    /*장바구니 목록 조회 */
    @ResponseBody
    @GetMapping("/api/cart")
    public ResponseEntity<List<CartItemEntity>> findCartItems() {

        Long memberId = getCurrentMemberId();

        List<CartItemEntity> cartItems =
                cartService.findCartItems(memberId);

        return ResponseEntity.ok(cartItems);
    }

    /*장바구니 상품 추가 */
    @ResponseBody
    @PostMapping("/api/cart/items")
    public ResponseEntity<String> addCartItem(
    		@RequestParam(name = "bookId") Long bookId,
            @RequestParam(name = "bookTitle") String bookTitle,
            @RequestParam(name = "price") int price,
            @RequestParam(name = "stock") int stock,
            @RequestParam(name = "quantity", defaultValue = "1") int quantity
    		) {
        Long memberId = getCurrentMemberId();

        cartService.addCartItem(
                memberId,
                bookId,
                bookTitle,
                price,
                stock,
                quantity
        );

        return ResponseEntity.ok(
                "장바구니 등록 완료"
        );
    }

 
}