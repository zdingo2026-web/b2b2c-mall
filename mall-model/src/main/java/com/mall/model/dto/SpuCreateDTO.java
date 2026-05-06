package com.mall.model.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 新建SPU参数
 */
@Data
public class SpuCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private Long brandId;

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    private String subTitle;

    private String mainImage;

    private List<String> images;

    private String description;

    /** SKU列表 */
    @Valid
    @NotNull(message = "SKU列表不能为空")
    private List<SkuItem> skuList;

    /** 属性列表 */
    @Valid
    private List<AttributeItem> attributeList;

    @Data
    public static class SkuItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private String skuName;

        private String skuCode;

        private String specValues;

        @NotNull(message = "SKU价格不能为空")
        private BigDecimal price;

        private BigDecimal originalPrice;

        @NotNull(message = "SKU库存不能为空")
        private Integer stock;

        private String image;

        private BigDecimal weight;
    }

    /** 标签类型: 0-无 1-百亿补贴 2-品牌特卖 3-新品首发 4-以旧换新 */
    private Integer tagType;

    /** 划线价/原价 */
    private BigDecimal originalPrice;

    @Data
    public static class AttributeItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotNull(message = "属性ID不能为空")
        private Long attributeId;

        /** 属性名称(冗余) */
        private String attributeName;

        @NotBlank(message = "属性值不能为空")
        private String attributeValue;

        /** 属性类型: 1规格 2参数 */
        private Integer attributeType;
    }
}
