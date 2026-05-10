package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DistributorRankVO {
    private Long distributorId;
    private String realName;
    private String phone;
    private BigDecimal totalCommission;
    private Integer orderCount;
}
