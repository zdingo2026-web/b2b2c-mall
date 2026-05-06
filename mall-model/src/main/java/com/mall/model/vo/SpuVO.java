package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表展示VO
 */
@Data
public class SpuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long categoryId;

    private Long brandId;

    private String productName;

    private String subTitle;

    private String mainImage;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer totalStock;

    private Integer totalSales;

    private Integer status;

    private Long tenantId;

    /** 商户名称 */
    private String tenantName;

    /** 分类名称 */
    private String categoryName;

    /** 品牌名称 */
    private String brandName;

    /** 标签类型: 0-无 1-百亿补贴 2-品牌特卖 3-新品首发 4-以旧换新 */
    private Integer tagType;

    /** 划线价/原价 */
    private BigDecimal originalPrice;

    private LocalDateTime createTime;
}
