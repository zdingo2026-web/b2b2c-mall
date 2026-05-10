package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String activityName;
    private Integer discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
