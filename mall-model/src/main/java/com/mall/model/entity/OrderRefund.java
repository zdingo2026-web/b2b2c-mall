package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 退换货表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderRefund extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 退款单号 */
    private String refundNo;

    /** 订单ID */
    private Long orderId;

    /** 订单明细ID */
    private Long orderItemId;

    /** 会员ID */
    private Long memberId;

    /** 退款类型: 1退款 2退货退款 */
    private Integer refundType;

    /** 退款原因 */
    private String refundReason;

    /** 退款描述 */
    private String refundDesc;

    /** 凭证图片(逗号分隔) */
    private String refundImages;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 状态: 0待审核 1已同意 2已拒绝 3已退款 4已关闭 */
    private Integer refundStatus;

    /** 退款申请前的订单状态(用于驳回时恢复) */
    private Integer preRefundStatus;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private java.time.LocalDateTime auditTime;

    /** 退款时间 */
    private java.time.LocalDateTime refundTime;
}
