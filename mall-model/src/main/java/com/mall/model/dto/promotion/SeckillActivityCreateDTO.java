package com.mall.model.dto.promotion;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeckillActivityCreateDTO {

    @NotBlank
    @Length(min = 2, max = 100)
    private String activityName;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    @Min(1)
    private Integer paymentTimeout;

    @NotEmpty
    @Valid
    private List<SeckillSkuDTO> skuList;
}
