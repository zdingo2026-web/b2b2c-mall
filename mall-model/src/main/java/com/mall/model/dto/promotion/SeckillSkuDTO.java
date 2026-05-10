package com.mall.model.dto.promotion;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class SeckillSkuDTO {

    @NotNull
    private Long spuId;

    @NotNull
    private Long skuId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal seckillPrice;

    @NotNull
    @Min(1)
    private Integer seckillStock;

    @NotNull
    @Min(1)
    private Integer limitPerUser;

    private Boolean canUseCoupon = false;
}
