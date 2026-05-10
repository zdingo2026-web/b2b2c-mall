package com.mall.model.vo.tenant;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantDashboardVO {
    private BigDecimal todaySales;
    private BigDecimal yesterdaySales;
    private Integer todayOrders;
    private Integer yesterdayOrders;
    private Integer pendingShipOrders;
    private Integer pendingRefundOrders;
    private Integer totalProducts;
    private Integer totalMembers;
}
