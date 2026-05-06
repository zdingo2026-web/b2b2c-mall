package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家分类表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 父分类ID */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 分类图标URL */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;
}
