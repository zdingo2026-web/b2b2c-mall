package com.mall.model.entity.points;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_detail")
public class PointsDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("biz_type")
    private Integer bizType;

    @TableField("biz_id")
    private String bizId;

    @TableField("change_amount")
    private Integer changeAmount;

    @TableField("change_type")
    private Integer changeType;

    @TableField("balance_after")
    private Integer balanceAfter;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("expired")
    private Integer expired;

    @TableField("remark")
    private String remark;
}
