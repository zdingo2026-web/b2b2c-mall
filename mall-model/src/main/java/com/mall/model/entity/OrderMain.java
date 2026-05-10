package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 主订单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderMain extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 订单编号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 父订单ID(拆单时，0为主订单) */
    private Long parentId;

    /** 订单类型: 1普通订单 */
    private Integer orderType;

    /** 关联活动ID */
    @TableField("activity_id")
    private Long activityId;

    /** 拼团记录ID */
    @TableField("group_record_id")
    private Long groupRecordId;

    /** 商户优惠券ID */
    @TableField("merchant_coupon_id")
    private Long merchantCouponId;

    /** 平台优惠券ID */
    @TableField("platform_coupon_id")
    private Long platformCouponId;

    /** 商户优惠券优惠金额 */
    @TableField("merchant_coupon_discount")
    private BigDecimal merchantCouponDiscount;

    /** 平台优惠券优惠金额 */
    @TableField("platform_coupon_discount")
    private BigDecimal platformCouponDiscount;

    /** 首单优惠金额 */
    @TableField("first_order_discount")
    private BigDecimal firstOrderDiscount;

    /** 积分抵扣金额 */
    @TableField("points_deduct_amount")
    private BigDecimal pointsDeductAmount;

    /** 积分抵扣数量 */
    @TableField("points_deduct_value")
    private Integer pointsDeductValue;

    /** 红包ID */
    @TableField("red_packet_id")
    private Long redPacketId;

    /** 红包优惠金额 */
    @TableField("red_packet_discount")
    private BigDecimal redPacketDiscount;

    /** 订单状态: 0待付款 1待发货 2待收货 3已完成 4已取消 5退款中 6已退款 */
    private Integer orderStatus;

    /** 支付状态: 0待支付 1已支付 2支付失败 3退款中 4已退款 5已关闭 */
    private Integer payStatus;

    /** 商品总金额 */
    private BigDecimal totalAmount;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 支付方式: 1微信 2支付宝 3余额 */
    private Integer payType;

    /** 支付时间 */
    private java.time.LocalDateTime payTime;

    /** 配送方式: 1快递 2自提 */
    private Integer deliveryType;

    /** 物流公司 */
    private String logisticsCompany;

    /** 物流单号 */
    private String logisticsNo;

    /** 发货时间 */
    private java.time.LocalDateTime deliveryTime;

    /** 收货时间 */
    private java.time.LocalDateTime receiveTime;

    /** 订单备注 */
    private String remark;

    /** 取消原因 */
    private String cancelReason;

    /** 订单超时时间(未支付自动取消) */
    private java.time.LocalDateTime expireTime;

    /** 版本号(乐观锁) */
    private Integer version;

    /** 店铺名称快照 */
    @TableField("tenant_name")
    private String tenantName;

    /** 是否已评价: 0-否 1-是 */
    @TableField("is_reviewed")
    private Integer isReviewed;

    /** 发票类型: 0-无 1-电子发票 2-纸质发票 */
    @TableField("invoice_type")
    private Integer invoiceType;

    /** 发票抬头 */
    @TableField("invoice_title")
    private String invoiceTitle;

    /** 物流单号(发货后填写) */
    @TableField("delivery_no")
    private String deliveryNo;

    /** 物流公司(发货后填写) */
    @TableField("delivery_company")
    private String deliveryCompany;

    /** 物流状态: 0-无 1-已发货 2-运输中 3-已签收 */
    @TableField("delivery_status")
    private Integer deliveryStatus;
}
