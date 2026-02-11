package com.bootcamp.paymentdemo.domain.webhook.controller;

import com.bootcamp.paymentdemo.domain.webhook.dto.WebhookRequest;
import com.bootcamp.paymentdemo.domain.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/portone")
    public ResponseEntity<Void> handlePortOneWebhook(
            @RequestHeader(value = "webhook-id", required = false) String webhookId,
            @RequestBody WebhookRequest request
    ) {
        log.info("V2 웹훅 수신 확인 - paymentId: {}, status: {}", request.getPaymentId(), request.getStatus());

        // webhookId가 없는 경우 paymentId를 대체 키로 사용
        String effectiveId = (webhookId != null) ? webhookId : "test-" + request.getPaymentId();

        webhookService.process(effectiveId, request);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/portone")
//    public ResponseEntity<Void> handlePortOneWebhook(
//            @RequestBody String rawJson
//    ) {
//        log.info("실제 JSON 데이터: {}", rawJson);
//
//        return ResponseEntity.ok().build();
//    }
}
