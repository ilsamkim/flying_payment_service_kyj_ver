package com.bootcamp.paymentdemo.domain.webhook.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookRequest {

    private String type;
    private String timestamp;
    private Data data;

    @Getter
    @NoArgsConstructor
    public static class Data {
        private String transactionId;
        private String paymentId;
        private String storeId;
    }

    public String getPaymentId() {
        return (data != null) ? data.getPaymentId() : null;
    }

    public String getStatus() {
        if (type.contains("Paid")) return "PAID";
        if (type.contains("Cancelled")) return "CANCELLED";

        return type;
    }
}
