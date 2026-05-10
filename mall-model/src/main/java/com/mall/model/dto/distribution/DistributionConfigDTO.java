package com.mall.model.dto.distribution;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class DistributionConfigDTO {
    private Boolean enabled;
    private Boolean autoAudit;
    @NotNull private Integer commissionBase;
    @NotNull @DecimalMin("0") @DecimalMax("30") private BigDecimal rateLevel1;
    @NotNull @DecimalMin("0") @DecimalMax("30") private BigDecimal rateLevel2;
    @NotNull @DecimalMin("0") @DecimalMax("30") private BigDecimal rateLevel3;
    @NotNull @DecimalMin("0") private BigDecimal minWithdraw;
    @NotNull @Min(1) private Integer freezeDays;
    @DecimalMin("0") private BigDecimal dailyWithdrawLimit;
    @NotBlank private String withdrawMethods;
}
