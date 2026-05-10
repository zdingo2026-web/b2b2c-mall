package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SeckillProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long activityId;
    private Long spuId;
    private Long skuId;
    private String spuName;
    private String mainImage;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer seckillSales;
    private Integer limitPerUser;
    private Boolean canUseCoupon;
}
