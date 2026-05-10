package com.mall.model.entity.member;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_red_packet")
public class MemberRedPacket extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("batch_id")
    private Long batchId;

    @TableField("member_id")
    private Long memberId;

    @TableField("face_value")
    private BigDecimal faceValue;

    @TableField("min_amount")
    private BigDecimal minAmount;

    @TableField("source_type")
    private Integer sourceType;

    @TableField("order_id")
    private Long orderId;

    @TableField("status")
    private Integer status;

    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    @TableField("use_time")
    private LocalDateTime useTime;
}
