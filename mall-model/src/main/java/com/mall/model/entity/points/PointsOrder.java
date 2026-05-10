package com.mall.model.entity.points;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_order")
public class PointsOrder extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("order_no")
    private String orderNo;

    @TableField("member_id")
    private Long memberId;

    @TableField("product_id")
    private Long productId;

    @TableField("product_name")
    private String productName;

    @TableField("product_image")
    private String productImage;

    @TableField("exchange_type")
    private Integer exchangeType;

    @TableField("points_amount")
    private Integer pointsAmount;

    @TableField("cash_amount")
    private BigDecimal cashAmount;

    @TableField("consignee_name")
    private String consigneeName;

    @TableField("consignee_phone")
    private String consigneePhone;

    @TableField("consignee_address")
    private String consigneeAddress;

    @TableField("status")
    private Integer status;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("ship_time")
    private LocalDateTime shipTime;

    @TableField("receive_time")
    private LocalDateTime receiveTime;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;
}
