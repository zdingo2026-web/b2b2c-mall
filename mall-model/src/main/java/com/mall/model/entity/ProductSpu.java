package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * SPU表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSpu extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long categoryId;

    /** 品牌ID */
    private Long brandId;

    /** 商品名称 */
    private String productName;

    /** 商品副标题 */
    private String subTitle;

    /** 主图URL */
    private String mainImage;

    /** 图片列表(逗号分隔) */
    private String images;

    /** 商品描述(富文本) */
    private String description;

    /** 最低价格 */
    private BigDecimal minPrice;

    /** 最高价格 */
    private BigDecimal maxPrice;

    /** 总库存 */
    private Integer totalStock;

    /** 总销量 */
    private Integer totalSales;

    /** 状态: 0草稿 1上架 2下架 3违规下架 */
    private Integer status;

    /** 排序 */
    private Integer sortOrder;

    /** 审核状态: 0待审核 1已通过 2已拒绝 */
    private Integer auditStatus;

    /** 审核备注 */
    private String auditRemark;

    /** 标签类型: 0-无 1-百亿补贴 2-品牌特卖 3-新品首发 4-以旧换新 */
    @TableField("tag_type")
    private Integer tagType;

    /** 划线价/原价 */
    @TableField("original_price")
    private BigDecimal originalPrice;
}
