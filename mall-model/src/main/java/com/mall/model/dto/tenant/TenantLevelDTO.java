package com.mall.model.dto.tenant;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TenantLevelDTO {
    @NotBlank @Length(max = 50) private String levelName;
    private String levelIcon;
    @NotNull @DecimalMin("0.1") @DecimalMax("1.0") private BigDecimal commissionDiscount;
    @Min(0) private Integer minScore = 0;
    private Integer sortWeight = 0;
}
