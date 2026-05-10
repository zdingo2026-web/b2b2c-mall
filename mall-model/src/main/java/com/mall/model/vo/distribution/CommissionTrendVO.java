package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionTrendVO {
    private String date;
    private BigDecimal commissionAmount;
    private Integer orderCount;
}
