package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FirstOrderConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Boolean enabled;
    private Integer discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private Integer applyScope;
    private String applyCategoryIds;
}
