package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商户首页看板 VO
 */
@Data
public class MerchantOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 今日订单数 */
    private Integer todayOrderCount;

    /** 今日订单金额 */
    private BigDecimal todayOrderAmount;

    /** 今日客单价 */
    private BigDecimal todayAvgOrderPrice;

    /** 昨日订单数 */
    private Integer yesterdayOrderCount;

    /** 昨日订单金额 */
    private BigDecimal yesterdayOrderAmount;

    /** 昨日客单价 */
    private BigDecimal yesterdayAvgOrderPrice;

    /** 累计订单金额 */
    private BigDecimal totalOrderAmount;

    /** 店铺转化率 */
    private BigDecimal shopConversionRate;
}
