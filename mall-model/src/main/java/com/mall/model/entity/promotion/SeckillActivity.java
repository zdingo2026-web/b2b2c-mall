package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 秒杀活动表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_activity")
public class SeckillActivity extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("activity_name")
    private String activityName;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("payment_timeout")
    private Integer paymentTimeout;

    @TableField("status")
    private Integer status;
}
