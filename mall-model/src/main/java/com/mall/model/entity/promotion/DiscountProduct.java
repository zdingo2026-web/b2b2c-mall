package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 折扣活动商品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("discount_product")
public class DiscountProduct extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("activity_id")
    private Long activityId;

    @TableField("spu_id")
    private Long spuId;
}
