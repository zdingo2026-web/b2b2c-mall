package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponTemplateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String tenantName;
    private String couponName;
    private Integer couponType;
    private BigDecimal couponValue;
    private BigDecimal minAmount;
    private BigDecimal maxDiscount;
    private Integer totalCount;
    private Integer remainCount;
    private Integer limitPerUser;
    private Integer issuerType;
    private Integer applyScope;
    private Integer validType;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private Integer validDays;
    private Boolean canStackSeckill;
    private Integer status;
    private LocalDateTime createTime;
}
