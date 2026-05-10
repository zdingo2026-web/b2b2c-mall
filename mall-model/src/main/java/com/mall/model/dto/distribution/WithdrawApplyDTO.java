package com.mall.model.dto.distribution;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class WithdrawApplyDTO {
    @NotNull @DecimalMin("100") private BigDecimal amount;
    @NotBlank private String withdrawMethod;
    @NotBlank private String accountInfo;
}
