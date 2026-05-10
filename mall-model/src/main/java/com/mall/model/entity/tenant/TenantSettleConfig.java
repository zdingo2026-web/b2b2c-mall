package com.mall.model.entity.tenant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_settle_config")
public class TenantSettleConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("enabled")
    private Integer enabled;

    @TableField("settle_notice")
    private String settleNotice;

    @TableField("settle_agreement")
    private String settleAgreement;

    @TableField("auto_audit")
    private Integer autoAudit;
}
