package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分类树节点VO
 */
@Data
public class CategoryTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    private String categoryName;

    private String icon;

    private String image;

    private Integer sortOrder;

    private Integer level;

    private Integer status;

    private List<CategoryTreeVO> children;
}
