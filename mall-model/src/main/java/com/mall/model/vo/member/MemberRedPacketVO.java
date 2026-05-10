package com.mall.model.vo.member;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberRedPacketVO {
    private Long id;
    private Long batchId;
    private BigDecimal faceValue;
    private BigDecimal minAmount;
    private Integer sourceType;
    private Integer status;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
}
