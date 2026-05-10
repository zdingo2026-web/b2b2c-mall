package com.mall.model.dto.promotion;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GrouponCreateDTO {

    @NotBlank
    @Length(min = 2, max = 100)
    private String activityName;

    @NotNull
    private Long spuId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal grouponPrice;

    @Min(0)
    private Integer limitPerUser;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotEmpty
    private List<GrouponProductDTO> skuList;
}
