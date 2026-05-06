package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 品牌表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductBrand extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 品牌名称 */
    private String brandName;

    /** 品牌Logo */
    private String logo;

    /** 品牌描述 */
    private String description;

    /** 排序 */
    private Integer sortOrder;

    /** 状态: 0禁用 1正常 */
    private Integer status;
}
