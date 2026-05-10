package com.mall.model.dto.promotion;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class GrouponProductDTO {

    @NotNull
    private Long skuId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal grouponPrice;

    @NotNull
    @Min(1)
    private Integer stock;

    private Integer sortOrder = 0;
}
