package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CouponOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long memberCouponId;
    private String couponName;
    private Integer couponType;
    private BigDecimal couponValue;
    private BigDecimal minAmount;
    private BigDecimal discountAmount;
    private Boolean applicable;
    private String rejectReason;
}
