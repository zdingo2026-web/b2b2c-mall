package com.mall.model.vo.points;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PointsOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer exchangeType;
    private Integer pointsAmount;
    private BigDecimal cashAmount;
    private Integer status;
    private LocalDateTime createTime;
}
