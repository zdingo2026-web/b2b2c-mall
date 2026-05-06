package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家分类 DTO
 */
@Data
public class TenantCategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 父分类ID */
    @NotNull(message = "父分类ID不能为空")
    private Long parentId;

    /** 分类名称 */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /** 分类图标URL */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;
}
