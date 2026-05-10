package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购活动表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("groupon_activity")
public class GrouponActivity extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("activity_name")
    private String activityName;

    @TableField("spu_id")
    private Long spuId;

    @TableField("groupon_price")
    private BigDecimal grouponPrice;

    @TableField("limit_per_user")
    private Integer limitPerUser;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private Integer status;
}
