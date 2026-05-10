package com.mall.model.dto.promotion;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponTemplateCreateDTO {

    @NotBlank
    @Length(min = 2, max = 50)
    private String couponName;

    @NotNull
    private Integer couponType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal couponValue;

    private BigDecimal minAmount;

    private BigDecimal maxDiscount;

    private Long giftSkuId;

    @NotNull
    @Min(1)
    private Integer totalCount;

    @NotNull
    @Min(1)
    private Integer limitPerUser;

    @NotNull
    private Integer applyScope;

    private String applyCategoryIds;

    private String applySpuIds;

    @NotNull
    private Integer validType;

    private LocalDateTime validStartTime;

    private LocalDateTime validEndTime;

    private Integer validDays;

    private Boolean canStackSeckill = false;
}
