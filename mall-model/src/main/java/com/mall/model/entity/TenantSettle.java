package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 结算表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantSettle extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 结算单号 */
    private String settleNo;

    /** 结算周期开始 */
    private java.time.LocalDateTime periodStart;

    /** 结算周期结束 */
    private java.time.LocalDateTime periodEnd;

    /** 订单金额 */
    private BigDecimal orderAmount;

    /** 佣金金额 */
    private BigDecimal commissionAmount;

    /** 实际结算金额 */
    private BigDecimal settleAmount;

    /** 状态: 0待结算 1已结算 */
    private Integer status;
}
