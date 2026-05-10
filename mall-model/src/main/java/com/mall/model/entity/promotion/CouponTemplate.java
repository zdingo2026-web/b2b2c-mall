package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_template")
public class CouponTemplate extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("coupon_name")
    private String couponName;

    @TableField("coupon_type")
    private Integer couponType;

    @TableField("coupon_value")
    private BigDecimal couponValue;

    @TableField("min_amount")
    private BigDecimal minAmount;

    @TableField("max_discount")
    private BigDecimal maxDiscount;

    @TableField("gift_sku_id")
    private Long giftSkuId;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("remain_count")
    private Integer remainCount;

    @TableField("limit_per_user")
    private Integer limitPerUser;

    @TableField("issuer_type")
    private Integer issuerType;

    @TableField("apply_scope")
    private Integer applyScope;

    @TableField("apply_category_ids")
    private String applyCategoryIds;

    @TableField("apply_spu_ids")
    private String applySpuIds;

    @TableField("valid_type")
    private Integer validType;

    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    @TableField("valid_days")
    private Integer validDays;

    @TableField("can_stack_seckill")
    private Integer canStackSeckill;

    @TableField("activity_id")
    private Long activityId;

    @TableField("status")
    private Integer status;
}
