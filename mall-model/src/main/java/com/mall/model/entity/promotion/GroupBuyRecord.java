package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 拼团记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_buy_record")
public class GroupBuyRecord extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("activity_id")
    private Long activityId;

    @TableField("head_member_id")
    private Long headMemberId;

    @TableField("group_status")
    private Integer groupStatus;

    @TableField("current_num")
    private Integer currentNum;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("success_time")
    private LocalDateTime successTime;

    @TableField("fail_time")
    private LocalDateTime failTime;
}
