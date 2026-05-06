package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCategory extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 父分类ID(0为顶级) */
    private Long parentId;

    /** 分类名称 */
    private String categoryName;

    /** 分类图标 */
    private String icon;

    /** 分类图片 */
    private String image;

    /** 排序 */
    private Integer sortOrder;

    /** 层级: 1一级 2二级 3三级 */
    private Integer level;

    /** 状态: 0禁用 1正常 */
    private Integer status;
}
