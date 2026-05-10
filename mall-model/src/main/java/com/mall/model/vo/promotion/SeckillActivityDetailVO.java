package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeckillActivityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String activityName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer paymentTimeout;
    private Integer status;
    private List<SeckillSkuVO> skuList;

    @Data
    public static class SeckillSkuVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;
        private Long spuId;
        private Long skuId;
        private BigDecimal seckillPrice;
        private Integer seckillStock;
        private Integer seckillSales;
        private Integer limitPerUser;
        private Boolean canUseCoupon;
    }
}
