package com.mall.model.entity.points;

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
@TableName("points_rule")
public class PointsRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("rule_type")
    private Integer ruleType;

    @TableField("rule_name")
    private String ruleName;

    @TableField("points_value")
    private Integer pointsValue;

    @TableField("multiplier")
    private BigDecimal multiplier;

    @TableField("daily_limit")
    private Integer dailyLimit;

    @TableField("enabled")
    private Integer enabled;

    @TableField("sort_order")
    private Integer sortOrder;
}
