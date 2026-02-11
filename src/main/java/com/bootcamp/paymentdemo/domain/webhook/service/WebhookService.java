package com.bootcamp.paymentdemo.domain.webhook.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.service.OrderService;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentRepository;
import com.bootcamp.paymentdemo.domain.refund.service.RefundService;
import com.bootcamp.paymentdemo.domain.webhook.dto.WebhookRequest;
import com.bootcamp.paymentdemo.domain.webhook.entity.Webhook;
import com.bootcamp.paymentdemo.domain.webhook.entity.WebhookStatus;
import com.bootcamp.paymentdemo.domain.webhook.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final PortOneService portOneService;
    private final OrderService orderService;
    private final PaymentRepository paymentRepository;
    private final RefundService refundService;

    @Transactional
    public void process(String recWebhookId, WebhookRequest request) {
        // 멱등성 체크
        if (webhookRepository.existsByRecWebhookId(recWebhookId)) {
            log.info("중복된 웹훅 무시: {}", recWebhookId);
            return;
        }

        Webhook webhook = Webhook.register(recWebhookId, WebhookStatus.PENDING, null); // 임시로 null 넣음
        webhook.updateEventStatus(request.getStatus());
        webhookRepository.save(webhook);

        try {
            String paymentId = request.getPaymentId();

            // 웹훅 수신 데이터 확인
            log.info("수신된 웹훅 상태: {}, 이벤트 타입: {}", request.getStatus(), request.getType());

            // 결제 webhook
            if ("PAID".equals(request.getStatus())) {
                // 포트원을 통해 실제 결제 금액 조회
                long actualAmount = portOneService.getPaymentAmount(paymentId);

                Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(paymentId)
                        .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));

                // DB의 주문 정보 조회
                Order order = payment.getOrder();
                String orderNo = order.getOrderNo();

                // 금액 검증
                if (actualAmount == order.getTotalActualPrice()) {
                    orderService.completeOrder(orderNo);
                    log.info("결제 검증 성공: paymentId {}, orderNo {}", paymentId, orderNo);
                } else {
                    log.error("결제 검증 실패: 금액 불일치 (실제: {}, 예상: {})", actualAmount, order.getTotalActualPrice());
                }

                // 환불 webhook
            } else if ("CANCELLED".equals(request.getStatus())) {
                try {
                    // 환불 로직 수행
                    refundService.processRefund(paymentId);
                    log.info("환불 처리 성공: paymentId {}", paymentId);
                } catch (Exception e) {
                    // 취소된 건이라는 예외가 발생하면 무시하고 정상 종료 처리
                    if (e.getMessage().contains("이미 환불") || e.getMessage().contains("CANCELLED")) {
                        log.info("이미 API를 통해 환불 처리된 건입니다. 웹훅 기록: {}", paymentId);
                    } else {
                        throw e;
                    }
                }
            }

            webhook.complete();

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생", e);
            throw e;
        }
    }
}
