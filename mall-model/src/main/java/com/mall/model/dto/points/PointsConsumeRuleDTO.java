package com.mall.model.dto.points;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PointsConsumeRuleDTO {

    @NotNull
    @DecimalMin("0.001")
    private BigDecimal exchangeRate;

    @NotNull
    @DecimalMin("1")
    @DecimalMax("100")
    private BigDecimal maxDeductRate;

    @NotNull
    private Integer validityType;

    private Integer validityDays;

    private String expireRemindDays;
}
