package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 首单优惠配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("first_order_config")
public class FirstOrderConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("enabled")
    private Integer enabled;

    @TableField("discount_type")
    private Integer discountType;

    @TableField("discount_value")
    private BigDecimal discountValue;

    @TableField("max_discount")
    private BigDecimal maxDiscount;

    @TableField("apply_scope")
    private Integer applyScope;

    @TableField("apply_category_ids")
    private String applyCategoryIds;
}
