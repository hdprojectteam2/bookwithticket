package com.example.bookwithticket.payment.dto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.example.bookwithticket.payment.exception.TossPaymentException;

@Component
public class TossPaymentClient {
	private static final String TOSS_CANCEL_URL = "https://api.tosspayments.com/v1/payments/";
	private final RestTemplate restTemplate;
	private final String secretKey;
	
	public TossPaymentClient(@Value("${toss.payments.secret-key}")String secretKey) {
		this.restTemplate = new RestTemplate();
		this.secretKey = secretKey;
	}
	
	public void cancelPayment(String paymentKey, String cancelReason) {
		String url = TOSS_CANCEL_URL + paymentKey + "/cancel";
		
		HttpHeaders headers = createHeaders();
		
		Map<String, Object> body = Map.of("cancelReason", cancelReason);
		
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
		
		try {
            restTemplate.postForEntity(
                    url,
                    entity,
                    String.class
            );

        } catch (HttpStatusCodeException exception) {

            throw new TossPaymentException("TOSS_CANCEL_FAILED", "토스 결제 취소에 실패했습니다.");
        }
	}
	
	private HttpHeaders createHeaders() {
		String authorization = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
		
		HttpHeaders headers = new HttpHeaders();
		
		headers.setBasicAuth(authorization);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
	}
}
