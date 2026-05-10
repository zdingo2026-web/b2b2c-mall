package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 限时折扣活动表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("discount_activity")
public class DiscountActivity extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("activity_name")
    private String activityName;

    @TableField("discount_type")
    private Integer discountType;

    @TableField("discount_value")
    private BigDecimal discountValue;

    @TableField("max_discount")
    private BigDecimal maxDiscount;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private Integer status;
}
