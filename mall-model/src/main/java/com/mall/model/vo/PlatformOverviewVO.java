package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 平台首页看板 VO
 */
@Data
public class PlatformOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 今日订单数 */
    private Integer todayOrderCount;

    /** 今日订单金额 */
    private BigDecimal todayOrderAmount;

    /** 今日新增会员 */
    private Integer todayNewMember;

    /** 今日访问量 */
    private Integer todayVisitCount;

    /** 昨日订单数 */
    private Integer yesterdayOrderCount;

    /** 昨日订单金额 */
    private BigDecimal yesterdayOrderAmount;
}
