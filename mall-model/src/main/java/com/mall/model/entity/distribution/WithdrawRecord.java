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
@TableName("withdraw_record")
public class WithdrawRecord extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("distributor_id")
    private Long distributorId;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("withdraw_method")
    private String withdrawMethod;

    @TableField("account_info")
    private String accountInfo;

    @TableField("status")
    private Integer status;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("auditor_id")
    private Long auditorId;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("payment_remark")
    private String paymentRemark;

    @TableField("payment_time")
    private LocalDateTime paymentTime;
}
