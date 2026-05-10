package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 拼团成员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_buy_member")
public class GroupBuyMember extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("group_record_id")
    private Long groupRecordId;

    @TableField("member_id")
    private Long memberId;

    @TableField("order_id")
    private Long orderId;

    @TableField("is_head")
    private Integer isHead;

    @TableField("join_time")
    private LocalDateTime joinTime;
}
