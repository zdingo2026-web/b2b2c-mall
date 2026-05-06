package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单操作日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人类型: 1会员 2商户 3平台 4系统 */
    private Integer operatorType;

    /** 操作人名称 */
    private String operatorName;

    /** 操作类型: 1创建 2支付 3发货 4收货 5取消 6退款 */
    private Integer operationType;

    /** 操作描述 */
    private String operationDesc;

    /** 订单状态(操作后) */
    private Integer orderStatus;
}
