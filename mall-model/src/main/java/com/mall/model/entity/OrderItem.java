package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单明细表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** SPU ID */
    private Long spuId;

    /** SKU ID */
    private Long skuId;

    /** 商品名称(快照) */
    private String productName;

    /** SKU规格(快照) */
    private String specValues;

    /** 商品图片(快照) */
    private String productImage;

    /** 单价(快照) */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 小计金额 */
    private BigDecimal subtotal;

    /** 实付金额 */
    private BigDecimal payAmount;
}
