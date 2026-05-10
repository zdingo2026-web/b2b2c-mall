package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DistributionCenterVO {
    private Boolean isDistributor;
    private Integer status;
    private BigDecimal totalCommission;
    private BigDecimal availableCommission;
    private BigDecimal frozenCommission;
    private Integer teamSize;
}
