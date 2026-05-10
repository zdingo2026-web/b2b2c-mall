package com.mall.model.vo.tenant;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantFreezeVO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Integer actionType;
    private String reason;
    private Boolean notifyMerchant;
    private LocalDateTime unfreezeTime;
    private LocalDateTime createTime;
}
