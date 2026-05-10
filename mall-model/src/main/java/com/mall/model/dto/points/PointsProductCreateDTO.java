package com.mall.model.dto.points;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PointsProductCreateDTO {

    private Long categoryId;

    @NotBlank
    @Length(max = 200)
    private String productName;

    @NotBlank
    private String productImage;

    private String productImages;

    private String description;

    @NotNull
    private Integer exchangeType;

    @NotNull
    @Min(1)
    private Integer pointsPrice;

    @DecimalMin("0")
    private BigDecimal cashPrice;

    private Integer mixedPoints;

    private BigDecimal mixedCash;

    @NotNull
    @Min(0)
    private Integer stock;

    private Integer sortOrder = 0;
}
