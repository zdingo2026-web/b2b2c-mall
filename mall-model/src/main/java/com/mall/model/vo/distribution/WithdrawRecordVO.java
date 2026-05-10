package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawRecordVO {
    private Long id;
    private BigDecimal amount;
    private String withdrawMethod;
    private Integer status;
    private String rejectReason;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}
