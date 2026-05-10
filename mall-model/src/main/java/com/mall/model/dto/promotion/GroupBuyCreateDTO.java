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
public class GroupBuyCreateDTO {

    @NotBlank
    @Length(min = 2, max = 100)
    private String activityName;

    @NotNull
    private Long spuId;

    private Long skuId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal groupPrice;

    @NotNull
    @Min(2)
    private Integer groupNum;

    @NotNull
    @Min(1)
    private Integer limitPerUser;

    @Min(0)
    private Integer maxGroups;

    @NotNull
    @Min(1)
    private Integer duration;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
