package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 团购商品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("groupon_product")
public class GrouponProduct extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("activity_id")
    private Long activityId;

    @TableField("spu_id")
    private Long spuId;

    @TableField("sku_id")
    private Long skuId;

    @TableField("groupon_price")
    private BigDecimal grouponPrice;

    @TableField("stock")
    private Integer stock;

    @TableField("sales")
    private Integer sales;

    @TableField("sort_order")
    private Integer sortOrder;
}
