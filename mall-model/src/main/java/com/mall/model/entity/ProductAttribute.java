package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 属性定义表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductAttribute extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long categoryId;

    /** 属性名称 */
    private String attributeName;

    /** 属性类型: 1规格 2参数 */
    private Integer attributeType;

    /** 输入类型: 0手工输入 1列表选择 */
    private Integer inputType;

    /** 可选值列表(逗号分隔) */
    private String selectableValues;

    /** 排序 */
    private Integer sortOrder;

    /** 状态: 0禁用 1正常 */
    private Integer status;
}
