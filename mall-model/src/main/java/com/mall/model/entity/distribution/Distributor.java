package com.mall.model.entity.distribution;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("distributor")
public class Distributor extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("member_id")
    private Long memberId;

    @TableField("real_name")
    private String realName;

    @TableField("phone")
    private String phone;

    @TableField("status")
    private Integer status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("total_commission")
    private BigDecimal totalCommission;

    @TableField("available_commission")
    private BigDecimal availableCommission;

    @TableField("frozen_commission")
    private BigDecimal frozenCommission;

    @TableField("parent_id")
    private Long parentId;

    @TableField("audit_time")
    private LocalDateTime auditTime;
}
