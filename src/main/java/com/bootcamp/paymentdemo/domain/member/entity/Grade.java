package com.bootcamp.paymentdemo.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Grade {
    DIAMOND(15, 300000),
    GOLD(10, 200000),
    SILVER(5, 100000),
    BRONZE(1, 0);

    private final int pointRate;
    private final int minAmount;

    public static Grade determineGrade(int totalAmount) {
        for (Grade grade : values()) {
            if(totalAmount >= grade.getMinAmount()) {
                return grade;
            }
        }
        return BRONZE;
    }
}
