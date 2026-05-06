package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品图片表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductImage extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** SPU ID */
    private Long spuId;

    /** 图片URL */
    private String imageUrl;

    /** 排序 */
    private Integer sortOrder;

    /** 图片类型: 1主图 2详情图 */
    private Integer imageType;
}
