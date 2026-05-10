package com.mall.model.entity.tenant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_commission_settlement")
public class TenantCommissionSettlement extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("settlement_no")
    private String settlementNo;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("period_start")
    private LocalDateTime periodStart;

    @TableField("period_end")
    private LocalDateTime periodEnd;

    @TableField("order_count")
    private Integer orderCount;

    @TableField("order_total_amount")
    private BigDecimal orderTotalAmount;

    @TableField("platform_commission")
    private BigDecimal platformCommission;

    @TableField("merchant_amount")
    private BigDecimal merchantAmount;

    @TableField("status")
    private Integer status;

    @TableField("settle_time")
    private LocalDateTime settleTime;
}
