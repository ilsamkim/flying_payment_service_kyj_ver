package com.bootcamp.paymentdemo.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING
    , COMPLETE
    , CANCELLED
}
