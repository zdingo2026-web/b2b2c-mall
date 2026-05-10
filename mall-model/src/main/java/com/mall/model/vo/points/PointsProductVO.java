package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PointsProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long categoryId;
    private String productName;
    private String productImage;
    private Integer exchangeType;
    private Integer pointsPrice;
    private BigDecimal cashPrice;
    private Integer stock;
    private Integer sales;
    private Integer status;
}
