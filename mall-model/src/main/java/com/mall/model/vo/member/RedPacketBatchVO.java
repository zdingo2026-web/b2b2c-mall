package com.mall.model.vo.member;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RedPacketBatchVO {
    private Long id;
    private String batchName;
    private Integer redPacketType;
    private BigDecimal faceValue;
    private BigDecimal minAmount;
    private Integer totalCount;
    private Integer claimedCount;
    private Integer usedCount;
    private Integer validType;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private Integer validDays;
    private Integer sendType;
    private Integer status;
}
