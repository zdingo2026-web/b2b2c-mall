package com.mall.model.vo.member;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberLevelVO {
    private Long id;
    private String levelName;
    private String levelIcon;
    private Integer requiredGrowth;
    private BigDecimal pointsMultiplier;
    private BigDecimal exclusiveDiscount;
    private Integer sortWeight;
}
