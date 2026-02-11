package com.bootcamp.paymentdemo.domain.payment.controller;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.domain.payment.dto.ConfirmPaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.dto.CreatePaymentRequest;
import com.bootcamp.paymentdemo.domain.payment.dto.CreatePaymentResponse;
import com.bootcamp.paymentdemo.domain.payment.service.PaymentService;
import com.bootcamp.paymentdemo.domain.refund.service.RefundService;
import com.bootcamp.paymentdemo.domain.webhook.service.PortOneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final PortOneService portOneService;

    //TODO Valid 처리는 어느정도 완성 이후 적용 예정
    @PostMapping
    public ResponseEntity<BaseResponse<CreatePaymentResponse>> createPayment(@RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), null, paymentService.createPayment(request)));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<BaseResponse<ConfirmPaymentResponse>> confirmPayment(
            @PathVariable String paymentId
            , @AuthenticationPrincipal UserDetails loginMemberInfo
            ) {
        String email = loginMemberInfo.getUsername();
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), null, paymentService.confirmPayment(paymentId, email)));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<BaseResponse<Void>> cancelPayment(
            @PathVariable String paymentId,
            @AuthenticationPrincipal UserDetails loginMemberInfo // 권한 확인이 필요한 경우 사용
    ) {
        log.info("결제 취소 요청 수신: paymentId {}", paymentId);

        // 포트원에 결제 취소 명령
        portOneService.cancelPayment(paymentId, "사용자 요청 환불");

        // RefundService를 호출하여 환불 로직 수행
        refundService.processRefund(paymentId);

        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.name(), "환불이 성공적으로 처리되었습니다.", null));
    }
}
