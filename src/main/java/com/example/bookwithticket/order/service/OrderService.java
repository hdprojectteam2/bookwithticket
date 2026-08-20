package com.example.bookwithticket.order.service;

import java.util.List;

import com.example.bookwithticket.order.dto.AdminOrderResponse;
import com.example.bookwithticket.order.dto.DeliveryRequest;
import com.example.bookwithticket.order.dto.DeliveryStatusRequest;
import com.example.bookwithticket.order.dto.OrderCreateRequest;
import com.example.bookwithticket.order.dto.OrderPageDto;
import com.example.bookwithticket.order.dto.OrderPreparedRequest;
import com.example.bookwithticket.order.dto.OrderPreparedResponse;
import com.example.bookwithticket.order.dto.OrderPreviewResponse;
import com.example.bookwithticket.order.dto.ShippingRequest;

public interface OrderService {

	OrderPageDto findPendingOrder(Long memberId, String orderNumber);

	void saveDelivery(Long memberId, String orderNumber, DeliveryRequest request);

	void cancelExpiredOrders();

	void cancelOrder(Long memberId, String orderNumber);

	List<AdminOrderResponse> findAdminOrders();
	
	void updateTrackingInfo(String orderNumber, ShippingRequest request);

	void updateDeliveryStatus(String orderNumber, DeliveryStatusRequest request);

	OrderPageDto findCompletedOrder(Long memberId, String orderNumber);

	OrderPreviewResponse previewOrder(Long memberId, OrderPreparedRequest request);

	OrderPreparedResponse createOrder(Long memberId, OrderCreateRequest request);
}