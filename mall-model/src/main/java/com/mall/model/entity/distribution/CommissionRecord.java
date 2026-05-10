package com.mall.model.entity.distribution;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("commission_record")
public class CommissionRecord extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("distributor_id")
    private Long distributorId;

    @TableField("order_id")
    private Long orderId;

    @TableField("order_item_id")
    private Long orderItemId;

    @TableField("order_no")
    private String orderNo;

    @TableField("order_amount")
    private BigDecimal orderAmount;

    @TableField("commission_rate")
    private BigDecimal commissionRate;

    @TableField("commission_amount")
    private BigDecimal commissionAmount;

    @TableField("commission_level")
    private Integer commissionLevel;

    @TableField("status")
    private Integer status;

    @TableField("unfreeze_time")
    private LocalDateTime unfreezeTime;

    @TableField("withdraw_id")
    private Long withdrawId;

    @TableField("cancel_reason")
    private String cancelReason;
}
