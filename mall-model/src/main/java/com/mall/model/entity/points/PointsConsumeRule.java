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
@TableName("points_consume_rule")
public class PointsConsumeRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("exchange_rate")
    private BigDecimal exchangeRate;

    @TableField("max_deduct_rate")
    private BigDecimal maxDeductRate;

    @TableField("validity_type")
    private Integer validityType;

    @TableField("validity_days")
    private Integer validityDays;

    @TableField("expire_remind_days")
    private String expireRemindDays;
}
