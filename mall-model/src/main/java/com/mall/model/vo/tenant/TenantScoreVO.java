package com.mall.model.vo.tenant;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TenantScoreVO {
    private Long tenantId;
    private String tenantName;
    private String logo;
    private Long levelId;
    private String levelName;
    private BigDecimal scoreProduct;
    private BigDecimal scoreService;
    private BigDecimal scoreLogistics;
    private BigDecimal scoreComposite;
    private Integer scoreCount;
    private Integer freezeStatus;
}
