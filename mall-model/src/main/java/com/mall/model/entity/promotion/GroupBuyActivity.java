package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_buy_activity")
public class GroupBuyActivity extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("activity_name")
    private String activityName;

    @TableField("spu_id")
    private Long spuId;

    @TableField("sku_id")
    private Long skuId;

    @TableField("group_price")
    private BigDecimal groupPrice;

    @TableField("group_num")
    private Integer groupNum;

    @TableField("limit_per_user")
    private Integer limitPerUser;

    @TableField("max_groups")
    private Integer maxGroups;

    @TableField("duration")
    private Integer duration;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private Integer status;
}
