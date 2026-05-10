package com.mall.model.vo.tenant;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryCommissionVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal rateLevel1;
    private BigDecimal rateLevel2;
    private BigDecimal rateLevel3;
}
