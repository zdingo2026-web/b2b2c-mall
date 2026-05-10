package com.mall.model.vo.member;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberCouponVO {
    private Long id;
    private Long couponTemplateId;
    private Long tenantId;
    private String couponName;
    private Integer couponType;
    private BigDecimal couponValue;
    private BigDecimal minAmount;
    private Integer sourceType;
    private Integer status;
    private LocalDateTime validStartTime;
    private LocalDateTime expireTime;
    private LocalDateTime useTime;
}
