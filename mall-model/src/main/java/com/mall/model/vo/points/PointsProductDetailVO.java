package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PointsProductDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long categoryId;
    private String productName;
    private String productImage;
    private String productImages;
    private String description;
    private Integer exchangeType;
    private Integer pointsPrice;
    private BigDecimal cashPrice;
    private Integer mixedPoints;
    private BigDecimal mixedCash;
    private Integer stock;
    private Integer sales;
    private Integer status;
}
