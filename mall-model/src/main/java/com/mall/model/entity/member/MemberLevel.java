package com.mall.model.entity.member;

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
@TableName("member_level")
public class MemberLevel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("level_name")
    private String levelName;

    @TableField("level_icon")
    private String levelIcon;

    @TableField("required_growth")
    private Integer requiredGrowth;

    @TableField("points_multiplier")
    private BigDecimal pointsMultiplier;

    @TableField("exclusive_discount")
    private BigDecimal exclusiveDiscount;

    @TableField("sort_weight")
    private Integer sortWeight;
}
