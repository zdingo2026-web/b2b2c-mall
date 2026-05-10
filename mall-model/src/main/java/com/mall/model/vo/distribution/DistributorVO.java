package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DistributorVO {
    private Long id;
    private Long memberId;
    private String realName;
    private String phone;
    private Integer status;
    private String rejectReason;
    private BigDecimal totalCommission;
    private BigDecimal availableCommission;
    private BigDecimal frozenCommission;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}
