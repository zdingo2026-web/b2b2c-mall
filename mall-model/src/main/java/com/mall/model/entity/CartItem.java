package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 购物车表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CartItem extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 会员ID */
    private Long memberId;

    /** SPU ID */
    private Long spuId;

    /** SKU ID */
    private Long skuId;

    /** 商品名称 */
    private String productName;

    /** SKU规格 */
    private String specValues;

    /** 商品图片 */
    private String productImage;

    /** 单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 是否选中: 0否 1是 */
    private Integer isChecked;
}
