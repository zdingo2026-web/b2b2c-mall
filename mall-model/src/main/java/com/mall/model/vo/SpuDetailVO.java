package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情VO(含SKU+属性+图片)
 */
@Data
public class SpuDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long categoryId;

    private Long brandId;

    private String productName;

    private String subTitle;

    private String mainImage;

    private List<String> images;

    private String description;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer totalStock;

    private Integer totalSales;

    private Integer status;

    private Long tenantId;

    private String tenantName;

    private String categoryName;

    private String brandName;

    private LocalDateTime createTime;

    /** SKU列表 */
    private List<SkuVO> skuList;

    /** 属性列表 */
    private List<AttributeValueVO> attributeList;

    /** 图片列表 */
    private List<ProductImageVO> imageList;

    /** 标签类型: 0-无 1-百亿补贴 2-品牌特卖 3-新品首发 4-以旧换新 */
    private Integer tagType;

    /** 划线价/原价 */
    private BigDecimal originalPrice;

    /** 优惠券提示: "领券立减XX元" */
    private String couponTag;

    /** 评价摘要 */
    private CommentSummaryVO commentSummary;

    /** 最新评价列表 */
    private List<ProductCommentVO> latestComments;

    @Data
    public static class SkuVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;

        private String skuName;

        private String skuCode;

        private String specValues;

        private BigDecimal price;

        private BigDecimal originalPrice;

        private Integer stock;

        private String image;

        private BigDecimal weight;

        private Integer status;
    }

    @Data
    public static class AttributeValueVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long attributeId;

        private String attributeName;

        private String attributeValue;

        private Integer attributeType;
    }

    @Data
    public static class ProductImageVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;

        private String imageUrl;

        private Integer sortOrder;

        private Integer imageType;
    }

    @Data
    public static class ProductCommentVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;

        private String nickname;

        private String avatar;

        private Integer score;

        private String content;

        private String images;

        private LocalDateTime createTime;
    }
}
