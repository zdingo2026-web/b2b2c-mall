package com.mall.model.vo.tenant;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TenantSettlementDetailVO {
    private Long id;
    private String settlementNo;
    private Long tenantId;
    private String tenantName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Integer orderCount;
    private BigDecimal orderTotalAmount;
    private BigDecimal platformCommission;
    private BigDecimal merchantAmount;
    private Integer status;
    private LocalDateTime settleTime;
}
