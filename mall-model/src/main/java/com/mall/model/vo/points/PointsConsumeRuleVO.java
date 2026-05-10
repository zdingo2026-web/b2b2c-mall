package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PointsConsumeRuleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private BigDecimal exchangeRate;
    private BigDecimal maxDeductRate;
    private Integer validityType;
    private Integer validityDays;
    private String expireRemindDays;
}
