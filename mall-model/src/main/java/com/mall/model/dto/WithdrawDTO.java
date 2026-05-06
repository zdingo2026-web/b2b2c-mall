package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提现申请参数
 */
@Data
public class WithdrawDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额不能低于0.01元")
    private BigDecimal amount;
}
