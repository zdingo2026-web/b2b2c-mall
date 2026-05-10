package com.mall.model.vo.distribution;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DistributionProductVO {
    private Long id;
    private Long spuId;
    private String spuName;
    private String mainImage;
    private BigDecimal price;
    private Boolean useGlobal;
    private BigDecimal rateLevel1;
    private BigDecimal rateLevel2;
    private BigDecimal rateLevel3;
    private Boolean canDistribute;
}
