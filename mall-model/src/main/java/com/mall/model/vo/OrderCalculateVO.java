package com.mall.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCalculateVO {
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal merchantCouponDiscount;
    private BigDecimal platformCouponDiscount;
    private BigDecimal firstOrderDiscount;
    private BigDecimal pointsDeductAmount;
    private Integer pointsDeductValue;
    private BigDecimal redPacketDiscount;
    private BigDecimal payAmount;
}
