package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退换货申请参数
 */
@Data
public class RefundApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 订单明细ID */
    @NotNull(message = "订单明细ID不能为空")
    private Long orderItemId;

    /** 退款类型: 1退款 2退货退款 */
    @NotNull(message = "退款类型不能为空")
    private Integer refundType;

    /** 退款原因 */
    private String refundReason;

    /** 退款描述 */
    private String refundDesc;

    /** 凭证图片(逗号分隔) */
    private String refundImages;

    /** 退款金额 */
    private BigDecimal refundAmount;
}
