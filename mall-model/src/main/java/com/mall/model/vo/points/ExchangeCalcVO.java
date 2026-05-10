package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ExchangeCalcVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long pointsProductId;
    private String exchangeType;
    private Integer requiredPoints;
    private BigDecimal requiredCash;
    private Boolean canAfford;
    private Integer pointsGap;
}
