package com.mall.model.entity.tenant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category_commission")
public class CategoryCommission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("category_id")
    private Long categoryId;

    @TableField("rate_level1")
    private BigDecimal rateLevel1;

    @TableField("rate_level2")
    private BigDecimal rateLevel2;

    @TableField("rate_level3")
    private BigDecimal rateLevel3;
}
