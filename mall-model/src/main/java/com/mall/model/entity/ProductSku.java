package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * SKU表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSku extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** SPU ID */
    private Long spuId;

    /** SKU名称 */
    private String skuName;

    /** SKU编码 */
    private String skuCode;

    /** 规格属性(JSON: {"颜色":"红","尺码":"XL"}) */
    private String specValues;

    /** 价格 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存 */
    private Integer stock;

    /** 锁定库存(下单未支付) */
    private Integer lockStock;

    /** 销量 */
    private Integer sales;

    /** 图片 */
    private String image;

    /** 重量(kg) */
    private java.math.BigDecimal weight;

    /** 状态: 0禁用 1正常 */
    private Integer status;
}
