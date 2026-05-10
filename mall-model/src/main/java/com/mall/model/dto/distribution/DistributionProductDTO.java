package com.mall.model.dto.distribution;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DistributionProductDTO {
    private Boolean useGlobal = true;
    private BigDecimal rateLevel1;
    private BigDecimal rateLevel2;
    private BigDecimal rateLevel3;
    private Boolean canDistribute = true;
}
