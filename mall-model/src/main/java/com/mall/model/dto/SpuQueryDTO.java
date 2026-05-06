package com.mall.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品查询参数
 */
@Data
public class SpuQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long categoryId;

    /** 品牌ID */
    private Long brandId;

    /** 关键词搜索 */
    private String keyword;

    /** 状态: 0草稿 1上架 2下架 3违规下架 */
    private Integer status;

    /** 最低价格 */
    private java.math.BigDecimal minPrice;

    /** 最高价格 */
    private java.math.BigDecimal maxPrice;

    /** 排序字段: price/sales/createTime */
    private String sortField;

    /** 排序方式: asc/desc */
    private String sortOrder;

    /** 页码 */
    private Integer page = 1;

    /** 每页数量 */
    private Integer limit = 10;
}
