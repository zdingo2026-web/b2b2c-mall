package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentRecord extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 支付单号 */
    private String paymentNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 支付方式: 1-微信 2-支付宝 3-余额 */
    private Integer payType;

    /** 支付金额 */
    private BigDecimal payAmount;

    /** 支付状态: 0-待支付 1-成功 2-失败 3-已关闭 */
    private Integer status;

    /** 第三方流水号 */
    @TableField("trade_no")
    private String tradeNo;

    /** 支付时间 */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /** 回调数据 */
    @TableField("callback_data")
    private String callbackData;

    /** 过期时间 */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
