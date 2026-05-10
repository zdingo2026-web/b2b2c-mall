package com.mall.model.dto.points;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PointsOrderCreateDTO {

    @NotNull
    private Long productId;

    @NotNull
    private Integer exchangeType;

    @NotNull
    private Long addressId;

    private Integer quantity = 1;
}
