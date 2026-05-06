package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentRefund extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 退款单号 */
    private String refundNo;

    /** 支付记录ID */
    private Long paymentId;

    /** 支付单号 */
    private String paymentNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /** 退款状态: 0-退款中 1-成功 2-失败 */
    private Integer status;

    /** 第三方流水号 */
    @TableField("trade_no")
    private String tradeNo;

    /** 退款时间 */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /** 回调数据 */
    @TableField("callback_data")
    private String callbackData;
}
