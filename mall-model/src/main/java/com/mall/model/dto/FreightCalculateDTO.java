package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 运费计算请求 DTO
 */
@Data
public class FreightCalculateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收货地址ID */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** 商品SKU列表 */
    @NotNull(message = "商品列表不能为空")
    private List<SkuItem> skuList;

    /**
     * SKU条目
     */
    @Data
    public static class SkuItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /** SKU ID */
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /** 数量 */
        @NotNull(message = "数量不能为空")
        private Integer quantity;

        /** 单价(用于计算满额包邮) */
        private BigDecimal price;

        /** 重量(kg, 按重量计费时使用) */
        private BigDecimal weight;

        /** 体积(m3, 按体积计费时使用) */
        private BigDecimal volume;
    }
}
