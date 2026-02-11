package com.bootcamp.paymentdemo.domain.order.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.payment.entity.Payment;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private LocalDateTime orderAt;

    @Column(nullable = false)
    private Integer totalOrderPrice;

    @Column(nullable = false)
    private Integer totalActualPrice;

    @Column(nullable = false)
    private Integer usePoint;

    @Column(nullable = false)
    private Integer savePoint;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private OrderCurrency currency;

    @Column(nullable = false)
    private boolean deleted;

    private LocalDateTime cancelAt;
    private LocalDateTime deletedAt;

    private Order(
            Member member
            , String orderNo
            , Integer totalOrderPrice
            , Integer totalActualPrice
            , Integer usePoint
            , Integer savePoint
    ) {
        this.member = member;
        this.orderAt = LocalDateTime.now();
        this.orderNo = orderNo;
        this.totalOrderPrice = totalOrderPrice;
        this.totalActualPrice = totalActualPrice;
        this.usePoint = usePoint;
        this.savePoint = savePoint;
        this.status = OrderStatus.PENDING;
        this.currency = OrderCurrency.KRW; // 아직은 원화 밖에 지원 안함
        this.cancelAt = null;
        this.deleted = false;
        this.deletedAt = null;
    }

    public static Order register(
            Member member
            , String orderNo
            , Integer totalOrderPrice
            , Integer totalActualPrice
            , Integer usePoint
            , Integer savePoint
    ) {
        return new Order(member
                , orderNo
                , totalOrderPrice
                , totalActualPrice
                , usePoint
                , savePoint
        );
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
        if(status == OrderStatus.CANCELLED) {
            this.deleted = true;
            this.deletedAt = LocalDateTime.now();
        }
    }

    // 실 결제 금액 업데이트
    public void updateActualPrice(Integer actualPrice) {
        this.totalActualPrice = actualPrice;
    }

    // 포인트 소모 업데이트
    public void updateSavePoint(Integer savePoint) {
        this.savePoint = savePoint;
    }

    // 포인트 소모 업데이트
    public void updateUsePoint(Integer usePoint) {
        this.usePoint = usePoint;
        this.totalActualPrice = totalOrderPrice - usePoint;
    }
}
