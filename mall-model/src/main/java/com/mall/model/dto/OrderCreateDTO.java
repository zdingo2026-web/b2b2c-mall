package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 创建订单参数
 */
@Data
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收货地址ID */
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    /** 配送方式: 1快递 2自提 */
    private Integer deliveryType = 1;

    /** 订单备注 */
    private String remark;

    /** 下单商品项(购物车下单可不传) */
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /** SKU ID */
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /** 数量 */
        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
