package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 楼层 VO (C端展示)
 */
@Data
public class FloorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 楼层名称 */
    private String floorName;

    /** 展示样式: 1-左右 2-上下 3-网格 */
    private Integer style;

    /** 楼层商品列表 */
    private List<SpuVO> products;

    /**
     * SPU简要信息VO
     */
    @Data
    public static class SpuVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;

        /** 商品名称 */
        private String productName;

        /** 副标题 */
        private String subTitle;

        /** 主图 */
        private String mainImage;

        /** 最低价格 */
        private java.math.BigDecimal minPrice;

        /** 销量 */
        private Integer totalSales;
    }
}
