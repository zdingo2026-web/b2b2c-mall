package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 秒杀商品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_sku")
public class SeckillSku extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("activity_id")
    private Long activityId;

    @TableField("spu_id")
    private Long spuId;

    @TableField("sku_id")
    private Long skuId;

    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @TableField("seckill_stock")
    private Integer seckillStock;

    @TableField("seckill_sales")
    private Integer seckillSales;

    @TableField("limit_per_user")
    private Integer limitPerUser;

    @TableField("can_use_coupon")
    private Integer canUseCoupon;

    @TableField("sort_order")
    private Integer sortOrder;
}
