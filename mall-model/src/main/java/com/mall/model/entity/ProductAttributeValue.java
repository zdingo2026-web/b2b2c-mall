package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 属性值表(SPU关联)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductAttributeValue extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** SPU ID */
    private Long spuId;

    /** 属性ID */
    private Long attributeId;

    /** 属性名称(冗余) */
    private String attributeName;

    /** 属性值 */
    private String attributeValue;

    /** 属性类型: 1规格 2参数 */
    private Integer attributeType;
}
