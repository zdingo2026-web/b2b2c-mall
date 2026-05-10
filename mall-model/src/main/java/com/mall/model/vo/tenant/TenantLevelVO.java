package com.mall.model.vo.tenant;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TenantLevelVO {
    private Long id;
    private String levelName;
    private String levelIcon;
    private BigDecimal commissionDiscount;
    private Integer minScore;
    private Integer sortWeight;
}
