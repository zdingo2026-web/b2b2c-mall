package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommissionRecordVO {
    private Long id;
    private Long distributorId;
    private String orderNo;
    private BigDecimal orderAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private Integer commissionLevel;
    private Integer status;
    private LocalDateTime unfreezeTime;
    private LocalDateTime createTime;
}
