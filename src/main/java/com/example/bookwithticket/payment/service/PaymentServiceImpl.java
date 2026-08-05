package com.example.bookwithticket.payment.service;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.example.bookwithticket.cart.repository.CartItemRepository;
import com.example.bookwithticket.order.entity.BookOrderEntity;
import com.example.bookwithticket.order.entity.OrderStatus;
import com.example.bookwithticket.order.repository.BookOrderRepository;
import com.example.bookwithticket.payment.dto.PaymentConfirmRequest;
import com.example.bookwithticket.payment.dto.PaymentConfirmResponse;
import com.example.bookwithticket.payment.dto.PaymentFailureRequest;
import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.entity.PaymentMethod;
import com.example.bookwithticket.payment.entity.PaymentStatus;
import com.example.bookwithticket.payment.exception.TossPaymentException;
import com.example.bookwithticket.payment.repository.PaymentRepository;
import com.example.bookwithticket.reservation.entity.ReservationEntity;
import com.example.bookwithticket.reservation.entity.ReservationStatus;
import com.example.bookwithticket.reservation.repository.ReservationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final PaymentRepository paymentRepository;
    private final BookOrderRepository bookOrderRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    private final String secretKey;

    private final CartItemRepository cartItemRepository;
    
    private final PaymentFailureService paymentFailureService;
    
    private final ReservationRepository reservationRepository;
    
    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BookOrderRepository bookOrderRepository,
            ObjectMapper objectMapper,
            @Value("${toss.payments.secret-key}")
            String secretKey,
            CartItemRepository cartItemRepository,
            PaymentFailureService paymentFailureService,
            ReservationRepository reservationRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.bookOrderRepository = bookOrderRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        this.secretKey = secretKey;
        this.cartItemRepository = cartItemRepository;
        this.paymentFailureService = paymentFailureService;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    @Override
    public PaymentConfirmResponse confirmPayment(Long memberId, PaymentConfirmRequest request) {
        /* 값 검사 */
    	validateRequest(request);
    	
    	String orderId = request.getOrderId();
    	
    	/* 도서, 공연 주문 번호 판별 */
    	if(orderId.startsWith("B")) {
    		return confirmBookPayment(memberId, request);
    	}
    	
    	if(orderId.startsWith("R")) {
    		return confirmPerformancePayment(memberId,request);
    	}
    	
    	throw new IllegalArgumentException("올바르지 않은 주문번호입니다.");
    }
    
    private PaymentConfirmResponse confirmBookPayment(Long memberId, PaymentConfirmRequest request) {
    	/* 주문번호, 회원번호, 결제 대기 상태 검사 */
        BookOrderEntity order =
                bookOrderRepository
                        .findByOrderNumberAndMemberIdAndOrderStatus(
                                request.getOrderId(),
                                memberId,
                                OrderStatus.PAYMENT_PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException("결제할 수 없는 주문입니다.")
                        );

        /* 배송지 입력 여부 검사 */
        if (order.getAddress() == null) {
            throw new IllegalArgumentException("배송지 정보가 없습니다.");
        }

        /* 프론트 & 서버 금액 검사*/
        if (order.getTotalPrice() != request.getAmount()) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        /* 중복 결제 검사 */
        if (paymentRepository.existsByPaymentKey(request.getPaymentKey())) {
            throw new IllegalArgumentException("이미 처리된 결제입니다.");
        }

        if (paymentRepository.existsByBookOrderIdAndStatus(order.getId(), PaymentStatus.DONE)) {
            throw new IllegalArgumentException("이미 결제가 완료된 주문입니다.");
        }

        
        String idempotencyKey =
                createIdempotencyKey(
                        order.getOrderNumber(),
                        request.getPaymentKey()
                );

        JsonNode tossResponse;

        try {
            tossResponse =
                    requestTossConfirmation(
                            request,
                            idempotencyKey
                    );

        } catch (TossPaymentException exception) {

            paymentFailureService.saveFailure(
                    order,
                    request.getPaymentKey(),
                    idempotencyKey,
                    order.getTotalPrice(),
                    exception.getCode(),
                    exception.getMessage()
            );

            throw exception;
        }

        String confirmedOrderId = tossResponse.path("orderId").asText();

        int confirmedAmount = tossResponse.path("totalAmount").asInt();

        String confirmedPaymentKey = tossResponse.path("paymentKey").asText();

        if (!order.getOrderNumber().equals(confirmedOrderId)) {
            throw new IllegalStateException( "주문번호가 일치하지 않습니다.");
        }

        if (order.getTotalPrice() != confirmedAmount) {
            throw new IllegalStateException("결제금액이 일치하지 않습니다.");
        }

        if (!request.getPaymentKey().equals(confirmedPaymentKey)) {
            throw new IllegalStateException("결제 키가 일치하지 않습니다.");
        }

        String tossMethod = tossResponse.path("method").asText();

        String easyPayProvider = tossResponse.path("easyPay").path("provider").asText(null);
        
        PaymentMethod paymentMethod = convertPaymentMethod(tossMethod, easyPayProvider);

        String approvedAtValue = tossResponse.path("approvedAt").asText();

        OffsetDateTime approvedAt = OffsetDateTime.parse(approvedAtValue);

        PaymentEntity payment = new PaymentEntity(order, confirmedPaymentKey, idempotencyKey, paymentMethod, confirmedAmount, approvedAt.toLocalDateTime());

        PaymentEntity savedPayment = paymentRepository.save(payment);

        /* 결제 성공 PAYMENT_PENDING - PAID */
        order.completePayment();
        
        cartItemRepository.deleteByCartMemberId(memberId);

        return new PaymentConfirmResponse(
                savedPayment.getId(),
                order.getOrderNumber(),
                savedPayment.getAmount(),
                savedPayment.getMethod().name(),
                savedPayment.getStatus().name()
        );
    }
    
    private PaymentConfirmResponse confirmPerformancePayment(Long memberId, PaymentConfirmRequest request) {
        ReservationEntity reservation =
                reservationRepository
                        .findByReservationNumberAndMemberIdAndStatus(
                                request.getOrderId(),
                                memberId,
                                ReservationStatus.PAYMENT_PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "결제할 수 없는 공연 예매입니다."
                                )
                        );

        /*
         * 프론트에서 전달한 금액과
         * DB 예매 금액 비교
         */
        if (reservation.getTotalPrice()!= request.getAmount()) {

            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        /* 같은 paymentKey가 이미 처리됐는지 확인 */
        if (paymentRepository.existsByPaymentKey(request.getPaymentKey())) {
            throw new IllegalArgumentException("이미 처리된 결제입니다.");
        }

        /* 해당 예매에 완료 결제가 이미 있는지 확인 */
        if (paymentRepository.existsByReservationIdAndStatus( reservation.getId(), PaymentStatus.DONE)) {
            throw new IllegalArgumentException("이미 결제가 완료된 예매입니다.");
        }

        String idempotencyKey =
                createIdempotencyKey(
                        reservation.getReservationNumber(),
                        request.getPaymentKey()
                );


        JsonNode tossResponse;

        try {
            tossResponse =
                    requestTossConfirmation(
                            request,
                            idempotencyKey
                    );

        } catch (TossPaymentException exception) {

            paymentFailureService.savePerformanceFailure(
                    reservation.getId(),
                    request.getPaymentKey(),
                    idempotencyKey,
                    reservation.getTotalPrice(),
                    exception.getCode(),
                    exception.getMessage()
            );

            throw exception;
        }
        
        
        String confirmedOrderId = tossResponse.path("orderId").asText();

        int confirmedAmount = tossResponse.path("totalAmount").asInt();

        String confirmedPaymentKey = tossResponse.path("paymentKey").asText();


        if (!reservation.getReservationNumber().equals(confirmedOrderId)) {

            throw new IllegalStateException("예매번호가 일치하지 않습니다.");
        }

        if (reservation.getTotalPrice() != confirmedAmount) {

            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

        if (!request.getPaymentKey().equals(confirmedPaymentKey)) {

            throw new IllegalStateException("결제 키가 일치하지 않습니다.");
        }

        String tossMethod = tossResponse.path("method").asText();

        String easyPayProvider = tossResponse.path("easyPay").path("provider").asText(null);

        PaymentMethod paymentMethod = convertPaymentMethod(tossMethod, easyPayProvider);

        String approvedAtValue = tossResponse.path("approvedAt").asText();

        OffsetDateTime approvedAt =
                OffsetDateTime.parse(
                        approvedAtValue
                );

        PaymentEntity payment =
                PaymentEntity.performancePayment(
                        reservation.getId(),
                        confirmedPaymentKey,
                        idempotencyKey,
                        paymentMethod,
                        confirmedAmount,
                        approvedAt.toLocalDateTime()
                );

        PaymentEntity savedPayment = paymentRepository.save(payment);


        reservation.completePayment();

        return new PaymentConfirmResponse(
                savedPayment.getId(),
                reservation.getReservationNumber(),
                savedPayment.getAmount(),
                savedPayment.getMethod().name(),
                savedPayment.getStatus().name()
        );
    }

    private JsonNode requestTossConfirmation(PaymentConfirmRequest request, String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set(HttpHeaders.AUTHORIZATION, createAuthorizationHeader());

        headers.set("Idempotency-Key", idempotencyKey);

        Map<String, Object> body = new HashMap<>();

        body.put("paymentKey", request.getPaymentKey());

        body.put("orderId", request.getOrderId());

        body.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            TOSS_CONFIRM_URL,
                            HttpMethod.POST,
                            httpEntity,
                            String.class
                    );

            return objectMapper.readTree(response.getBody());

        } catch (HttpStatusCodeException exception) {
        	String responseBody = exception.getResponseBodyAsString();

            String failCode = extractTossErrorCode(responseBody);

            String failMessage = extractTossErrorMessage(responseBody);

            System.out.println("토스 상태 코드: " + exception.getStatusCode());

            System.out.println("토스 응답: " + responseBody);

            throw new TossPaymentException(failCode, failMessage);

        } catch (Exception exception) {
            throw new IllegalStateException("결제 승인 처리 중 오류가 발생했습니다.", exception);
        }
    }

    private String createAuthorizationHeader() {
        String value = secretKey + ":";

        String encoded =
                Base64.getEncoder()
                        .encodeToString(
                                value.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        return "Basic " + encoded;
    }

    private String createIdempotencyKey(String orderNumber, String paymentKey) {
        return UUID.nameUUIDFromBytes(("PAYMENT:" + orderNumber + ":" + paymentKey).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private PaymentMethod convertPaymentMethod(String method, String easyPayProvider) {
        if ("계좌이체".equals(method)) {
            return PaymentMethod.ACCOUNT_TRANSFER;
        }

        if ("카드".equals(method)) {
            return PaymentMethod.CARD;
        }

        if ("간편결제".equals(method)) {
            return convertEasyPayProvider(easyPayProvider);
        }

        return PaymentMethod.UNKNOWN;
    }
    
    private PaymentMethod convertEasyPayProvider(String provider) {
        if ("토스페이".equals(provider)) {
            return PaymentMethod.TOSS_PAY;
        }

        if ("페이코".equals(provider)) {
            return PaymentMethod.PAYCO;
        }

        if ("카카오페이".equals(provider)) {
            return PaymentMethod.KAKAO_PAY;
        }

        if ("네이버페이".equals(provider)) {
            return PaymentMethod.NAVER_PAY;
        }
        
        if("SSG페이".equals(provider)) {
        	return PaymentMethod.SSG_PAY;
        }
        
        if("엘페이".equals(provider)) {
        	return PaymentMethod.L_PAY;
        }
        
        if("삼성페이".equals(provider)) {
        	return PaymentMethod.SAMSUNG_PAY;
        }
        
        return PaymentMethod.UNKNOWN;
    }

    private String extractTossErrorMessage(
            String responseBody
    ) {
        try {
            JsonNode error = objectMapper.readTree(responseBody);

            String message = error.path("message").asText();

            if (message != null && !message.isBlank()) {
                return message;
            }

        } catch (Exception ignored) {
        }

        return "결제 승인에 실패했습니다.";
    }

    private void validateRequest(PaymentConfirmRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("결제 정보가 없습니다.");
        }

        if (request.getPaymentKey() == null || request.getPaymentKey().isBlank()) {
            throw new IllegalArgumentException("결제 키가 없습니다.");
        }

        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new IllegalArgumentException("주문번호가 없습니다.");
        }

        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }
    }
    
    @Override
    public void savePaymentFailure(Long memberId, PaymentFailureRequest request) {
    	String orderId = request.getOrderId();

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문번호가 없습니다.");
        }

        if (orderId.startsWith("B")) {
            saveBookPaymentFailure(memberId, request);
            return;
        }

        if (orderId.startsWith("R")) {
            savePerformancePaymentFailure(memberId, request);
            return;
        }

        throw new IllegalArgumentException("올바르지 않은 주문번호입니다");
    }
    
    private void saveBookPaymentFailure(Long memberId, PaymentFailureRequest request) {
        BookOrderEntity order =
                bookOrderRepository
                        .findByOrderNumberAndMemberIdAndOrderStatus(
                                request.getOrderId(),
                                memberId,
                                OrderStatus.PAYMENT_PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException("실패 처리할 도서 주문이 없습니다.")
                        );

        String idempotencyKey = UUID.randomUUID().toString();

        paymentFailureService.saveFailure(
                order,
                null,
                idempotencyKey,
                order.getTotalPrice(),
                request.getCode(),
                request.getMessage()
        );
    }

    private void savePerformancePaymentFailure(Long memberId, PaymentFailureRequest request) {
        ReservationEntity reservation =
                reservationRepository
                        .findByReservationNumberAndMemberIdAndStatus(
                                request.getOrderId(),
                                memberId,
                                ReservationStatus.PAYMENT_PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException("실패 처리할 공연 예매가 없습니다.")
                        );

        String idempotencyKey = UUID.randomUUID().toString();

        paymentFailureService.savePerformanceFailure(
                reservation.getId(),
                null,
                idempotencyKey,
                reservation.getTotalPrice(),
                request.getCode(),
                request.getMessage()
        );
    }
    
    private String extractTossErrorCode(String responseBody) {
        try {
            JsonNode error =
                    objectMapper.readTree(
                            responseBody
                    );

            String code =
                    error.path("code").asText();

            if (code != null && !code.isBlank()) {
                return code;
            }

        } catch (Exception ignored) {
        }

        return "UNKNOWN_PAYMENT_ERROR";
    }
}