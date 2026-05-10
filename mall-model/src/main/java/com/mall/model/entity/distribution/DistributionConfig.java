package com.mall.model.entity.distribution;

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
@TableName("distribution_config")
public class DistributionConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("enabled")
    private Integer enabled;

    @TableField("auto_audit")
    private Integer autoAudit;

    @TableField("commission_base")
    private Integer commissionBase;

    @TableField("rate_level1")
    private BigDecimal rateLevel1;

    @TableField("rate_level2")
    private BigDecimal rateLevel2;

    @TableField("rate_level3")
    private BigDecimal rateLevel3;

    @TableField("min_withdraw")
    private BigDecimal minWithdraw;

    @TableField("freeze_days")
    private Integer freezeDays;

    @TableField("daily_withdraw_limit")
    private BigDecimal dailyWithdrawLimit;

    @TableField("withdraw_methods")
    private String withdrawMethods;
}
