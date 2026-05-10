package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PointsRuleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer ruleType;
    private String ruleName;
    private Integer pointsValue;
    private BigDecimal multiplier;
    private Integer dailyLimit;
    private Boolean enabled;
    private Integer sortOrder;
}
