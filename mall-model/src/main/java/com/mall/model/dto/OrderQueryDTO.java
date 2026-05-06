package com.mall.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单查询参数
 */
@Data
public class OrderQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单编号 */
    private String orderNo;

    /** 订单状态 */
    private Integer orderStatus;

    /** 支付状态 */
    private Integer payStatus;

    /** 起始时间 */
    private String beginTime;

    /** 结束时间 */
    private String endTime;

    /** 页码 */
    private Integer page = 1;

    /** 每页数量 */
    private Integer limit = 10;
}
