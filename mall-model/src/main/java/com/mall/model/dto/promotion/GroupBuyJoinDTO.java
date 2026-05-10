package com.mall.model.dto.promotion;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class GroupBuyJoinDTO {

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private Long addressId;
}
