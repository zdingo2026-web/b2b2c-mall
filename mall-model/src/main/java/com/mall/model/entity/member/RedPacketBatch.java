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
@TableName("red_packet_batch")
public class RedPacketBatch extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("batch_name")
    private String batchName;

    @TableField("red_packet_type")
    private Integer redPacketType;

    @TableField("face_value")
    private BigDecimal faceValue;

    @TableField("min_amount")
    private BigDecimal minAmount;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("claimed_count")
    private Integer claimedCount;

    @TableField("used_count")
    private Integer usedCount;

    @TableField("valid_type")
    private Integer validType;

    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    @TableField("valid_days")
    private Integer validDays;

    @TableField("send_type")
    private Integer sendType;

    @TableField("status")
    private Integer status;
}
