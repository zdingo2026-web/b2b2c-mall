package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DistributionOrderVO {
    private Long orderItemId;
    private String orderNo;
    private Long memberId;
    private String memberNickname;
    private BigDecimal orderAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private Integer commissionLevel;
    private Integer commissionStatus;
    private LocalDateTime createTime;
}
