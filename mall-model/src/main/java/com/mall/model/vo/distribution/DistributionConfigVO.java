package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DistributionConfigVO {
    private Long id;
    private Boolean enabled;
    private Boolean autoAudit;
    private Integer commissionBase;
    private BigDecimal rateLevel1;
    private BigDecimal rateLevel2;
    private BigDecimal rateLevel3;
    private BigDecimal minWithdraw;
    private Integer freezeDays;
    private BigDecimal dailyWithdrawLimit;
    private String withdrawMethods;
}
