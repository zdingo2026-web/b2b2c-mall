package com.mall.model.dto.member;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MemberLevelDTO {
    @NotBlank private String levelName;
    private String levelIcon;
    @NotNull @Min(0) private Integer requiredGrowth;
    private BigDecimal pointsMultiplier;
    private BigDecimal exclusiveDiscount;
    private Integer sortWeight = 0;
}
