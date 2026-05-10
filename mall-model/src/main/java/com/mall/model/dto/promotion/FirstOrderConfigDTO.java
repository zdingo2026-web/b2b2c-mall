package com.mall.model.dto.promotion;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class FirstOrderConfigDTO {

    private Boolean enabled;

    @NotNull
    private Integer discountType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal discountValue;

    private BigDecimal maxDiscount;

    @NotNull
    private Integer applyScope;

    private String applyCategoryIds;
}
