package com.mall.model.entity.tenant;

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
@TableName("tenant_freeze_record")
public class TenantFreezeRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("action_type")
    private Integer actionType;

    @TableField("reason")
    private String reason;

    @TableField("notify_merchant")
    private Integer notifyMerchant;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("unfreeze_time")
    private LocalDateTime unfreezeTime;
}
