package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单展示VO
 */
@Data
public class OrderMainVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long memberId;

    private Long parentId;

    private Integer orderType;

    private Integer orderStatus;

    private Integer payStatus;

    private BigDecimal totalAmount;

    private BigDecimal freightAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private Integer payType;

    private LocalDateTime payTime;

    private Integer deliveryType;

    private String logisticsCompany;

    private String logisticsNo;

    private LocalDateTime deliveryTime;

    private LocalDateTime receiveTime;

    private String remark;

    private LocalDateTime expireTime;

    private Long tenantId;

    /** 店铺名称快照 */
    private String tenantName;

    /** 是否已评价: 0-否 1-是 */
    private Integer isReviewed;

    /** 发票类型: 0-无 1-电子发票 2-纸质发票 */
    private Integer invoiceType;

    /** 发票抬头 */
    private String invoiceTitle;

    /** 物流单号 */
    private String deliveryNo;

    /** 物流公司 */
    private String deliveryCompany;

    /** 物流状态: 0-无 1-已发货 2-运输中 3-已签收 */
    private Integer deliveryStatus;

    private LocalDateTime createTime;

    /** 订单明细列表 */
    private List<OrderItemVO> items;

    /** 收货地址 */
    private OrderAddressVO address;

    @Data
    public static class OrderItemVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;

        private Long spuId;

        private Long skuId;

        private String productName;

        private String specValues;

        private String productImage;

        private BigDecimal price;

        private Integer quantity;

        private BigDecimal subtotal;

        private BigDecimal payAmount;
    }

    @Data
    public static class OrderAddressVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private String receiverName;

        private String receiverPhone;

        private String fullAddress;
    }
}
