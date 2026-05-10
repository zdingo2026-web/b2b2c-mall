package com.mall.model.dto.points;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PointsRuleDTO {

    @NotNull
    private Integer ruleType;

    @NotBlank
    @Length(max = 50)
    private String ruleName;

    @NotNull
    @Min(1)
    private Integer pointsValue;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal multiplier;

    @Min(0)
    private Integer dailyLimit;

    private Boolean enabled = true;

    private Integer sortOrder = 0;
}
