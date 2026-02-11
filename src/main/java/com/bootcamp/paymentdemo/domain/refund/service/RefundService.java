package com.bootcamp.paymentdemo.domain.refund.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;

import com.bootcamp.paymentdemo.domain.member.entity.Grade;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderStatus;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.order.repository.ProductOrderRepository;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentStatus;
import com.bootcamp.paymentdemo.domain.payment.repository.PaymentRepository;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLog;
import com.bootcamp.paymentdemo.domain.point.entity.MemberPointLogStatus;
import com.bootcamp.paymentdemo.domain.point.repository.MemberPointLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final MemberPointLogRepository memberPointLogRepository;
    private final ProductOrderRepository productOrderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRefund(String portOneId) {
        Payment payment = paymentRepository.findByPortOneIdAndDeletedFalse(portOneId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_PAYMENT));

        validateRefundAvailability(payment);

        Order order = payment.getOrder();
        Member member = order.getMember();

        // 상태 변경
        payment.updateStatus(PaymentStatus.CANCELLED);
        order.updateStatus(OrderStatus.CANCELLED);

        // 누적 결제 금액 차감
        member.subtractTotalPriceAmount(order.getTotalActualPrice());

        // 등급 재계산
        Grade newGrade = Grade.determineGrade(member.getTotalPriceAmount());
        member.updateGrade(newGrade);

        // 포인트 복구
        handlePointRefund(member, order);

        // 재고 복구
        List<ProductOrder> productOrders = productOrderRepository.findByOrder(order);
        for (ProductOrder po : productOrders) {
            // 주문 시 깎았던 수량만큼 -를 인자로 주어 증가시킴
            po.getProduct().updateStock(-po.getQuantity());
        }

        log.info("주문 취소 및 환불 프로세스 완료: orderNo {}", order.getOrderNo());
    }

    private void validateRefundAvailability(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new ServiceErrorException(ErrorEnum.ERR_ALREADY_CANCELLED);
        }

        if (payment.getStatus() != PaymentStatus.COMPLETE) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_REFUND_STATUS);
        }
    }

    private void handlePointRefund(Member member, Order order) {
        // 포인트 복구
        if (order.getUsePoint() > 0) {
            member.addPoint(order.getUsePoint());
            memberPointLogRepository.save(MemberPointLog.register(order.getOrderNo(), order.getUsePoint(), MemberPointLogStatus.RECOVER, member));
        }

        // 적립 취소
        if (order.getSavePoint() > 0) {
            member.minusPoint(order.getSavePoint());
            memberPointLogRepository.save(MemberPointLog.register(order.getOrderNo(), order.getSavePoint(), MemberPointLogStatus.CANCEL_EARN, member));
        }
    }
}
