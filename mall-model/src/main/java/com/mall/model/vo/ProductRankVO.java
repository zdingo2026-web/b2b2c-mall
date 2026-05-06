package com.mall.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品销量排行VO
 */
@Data
public class ProductRankVO {

    /** SPU ID */
    private Long spuId;

    /** 商品名称 */
    private String productName;

    /** 商品主图 */
    private String mainImage;

    /** 累计销量 */
    private Integer totalSales;

    /** 累计金额 */
    private BigDecimal totalAmount;
}
