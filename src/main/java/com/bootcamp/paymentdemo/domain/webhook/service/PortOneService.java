package com.bootcamp.paymentdemo.domain.webhook.service;


import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class PortOneService {

    @Value("${portone.api.base-url}")
    private String baseUrl;

    @Value("${portone.api.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public long getPaymentAmount(String paymentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = String.format("%s/payments/%s", baseUrl, paymentId);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> payment = (Map<String, Object>) response.getBody();
            Map<String, Object> amountMap = (Map<String, Object>) payment.get("amount");

            return Long.parseLong(amountMap.get("total").toString());
        } catch (Exception e) {
            log.error("포트원 API 호출 실패: {}", e.getMessage());
            throw new ServiceErrorException(ErrorEnum.ERR_WEBHOOK_NOT_FOUND_PAYMENT);
        }
    }
}
