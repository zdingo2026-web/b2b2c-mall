package com.mall.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户账户余额VO
 */
@Data
public class BalanceVO {

    /** 累计已结算 */
    private BigDecimal totalSettled;

    /** 待提现金额 */
    private BigDecimal pendingWithdrawal;

    /** 可用余额 */
    private BigDecimal availableBalance;
}
