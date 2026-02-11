package com.bootcamp.paymentdemo.domain.payment.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payments_port_one_id", columnNames = {"port_one_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, updatable = false, length = 120)
    private String portOneId;

    @Column(nullable = false, updatable = false)
    private Integer priceSnap;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime paymentAt;

    private LocalDateTime refundAt;

    @Column(nullable = false)
    private Boolean deleted;
    private LocalDateTime deletedAt;

    public static Payment register(Order order, Integer priceSnap) {
        Payment payment = new Payment();
        payment.order = order;
        payment.portOneId = String.format(
                "%s-%s-%s"
                , "PAY"
                , new SimpleDateFormat("yyyyMMdd").format(new Date())
                , UUID.randomUUID().toString().replace("-", "").substring(0, 12)
        );
        payment.priceSnap = priceSnap;
        payment.status = PaymentStatus.PENDING;
        payment.paymentAt = LocalDateTime.now();
        payment.refundAt = null;
        payment.deleted = false;
        return payment;
    }

    public void delete() {
        deleted = true;
        deletedAt = LocalDateTime.now();
    }

    public void restore() {
        deleted = false;
        deletedAt = null;
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
        if (status == PaymentStatus.CANCELLED) {
            this.refundAt = LocalDateTime.now();
        }
    }
}
