package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DistributionStatVO {
    private BigDecimal totalCommission;
    private BigDecimal availableCommission;
    private BigDecimal frozenCommission;
    private Integer totalOrders;
    private Integer totalDistributors;
}
