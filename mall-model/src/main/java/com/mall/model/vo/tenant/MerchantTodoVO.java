package com.mall.model.vo.tenant;

import lombok.Data;

@Data
public class MerchantTodoVO {
    private Integer pendingShip;
    private Integer pendingRefund;
    private Integer lowStockProducts;
    private Integer expiringActivities;
}
