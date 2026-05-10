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
@TableName("tenant_level")
public class TenantLevel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("level_name")
    private String levelName;

    @TableField("level_icon")
    private String levelIcon;

    @TableField("commission_discount")
    private BigDecimal commissionDiscount;

    @TableField("min_score")
    private Integer minScore;

    @TableField("sort_weight")
    private Integer sortWeight;
}
