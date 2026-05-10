package com.mall.model.vo.tenant;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantTrendVO {
    private String date;
    private BigDecimal salesAmount;
    private Integer orderCount;
}
