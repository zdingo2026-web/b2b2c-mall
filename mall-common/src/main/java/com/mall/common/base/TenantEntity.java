package com.mall.common.base;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Tenant-aware entity. All business tables extend this.
 * tenantId = 0 means platform-level data.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TenantEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField(value = "tenant_id")
    private Long tenantId;
}
