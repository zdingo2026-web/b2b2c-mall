package com.mall.model.entity.points;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_product")
public class PointsProduct extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("category_id")
    private Long categoryId;

    @TableField("product_name")
    private String productName;

    @TableField("product_image")
    private String productImage;

    @TableField("product_images")
    private String productImages;

    @TableField("description")
    private String description;

    @TableField("exchange_type")
    private Integer exchangeType;

    @TableField("points_price")
    private Integer pointsPrice;

    @TableField("cash_price")
    private BigDecimal cashPrice;

    @TableField("mixed_points")
    private Integer mixedPoints;

    @TableField("mixed_cash")
    private BigDecimal mixedCash;

    @TableField("stock")
    private Integer stock;

    @TableField("sales")
    private Integer sales;

    @TableField("status")
    private Integer status;

    @TableField("sort_order")
    private Integer sortOrder;
}
