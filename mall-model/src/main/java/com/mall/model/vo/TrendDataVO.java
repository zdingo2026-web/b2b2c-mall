package com.mall.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 趋势数据VO（按天）
 */
@Data
public class TrendDataVO {

    /** 日期 (MM-dd) */
    private String date;

    /** 订单数 */
    private Integer orderCount;

    /** 订单金额 */
    private BigDecimal orderAmount;
}
