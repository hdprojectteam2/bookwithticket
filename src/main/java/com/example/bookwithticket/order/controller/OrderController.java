package com.example.bookwithticket.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bookwithticket.order.dto.DeliveryRequest;
import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPreparedRequest;
import com.example.bookwithticket.order.dto.OrderPreparedResponse;
import com.example.bookwithticket.order.service.OrderService;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Long getCurrentMemberId() {
        /*임시 회원 ID */
        return 1L;
    }

    /* 장바구니에서 주문자 정보 입력 페이지 이동 시 임시 주문 생성 */
    @ResponseBody
    @PostMapping("/api/orders/prepare")
    public ResponseEntity<OrderPreparedResponse> prepareOrder(
            @RequestBody OrderPreparedRequest request
    ) {
        Long memberId = getCurrentMemberId();

        OrderPreparedResponse response =
                orderService.prepareOrder(
                        memberId,
                        request
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/order")
    public String deliveryPage(
            @RequestParam(name = "orderNumber")
            String orderNumber,
            Model model
    ) {
        Long memberId = getCurrentMemberId();

        OrderPageDto order =
                orderService.findPendingOrder(
                        memberId,
                        orderNumber
                );

        model.addAttribute("order", order);

        return "order/order";
    }

    /* 예외 처리 */
    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(exception.getMessage());
    }
    
    @ResponseBody
    @PutMapping("/api/orders/{orderNumber}/delivery")
    public ResponseEntity<String> saveDelivery(
    		@PathVariable(name = "orderNumber")
    		String orderNumber,
    		@RequestBody
    		DeliveryRequest request
    		){
    	Long memberId = getCurrentMemberId();
    	
    	orderService.saveDelivery(memberId, orderNumber, request);
    	return ResponseEntity.ok("배송지 정보 저장 완료");
    	
    }
}