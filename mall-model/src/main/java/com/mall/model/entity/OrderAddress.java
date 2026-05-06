package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 地址快照表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderAddress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人手机号 */
    private String receiverPhone;

    /** 省名称 */
    private String provinceName;

    /** 市名称 */
    private String cityName;

    /** 区名称 */
    private String districtName;

    /** 详细地址 */
    private String detailAddress;

    /** 完整地址(拼接) */
    private String fullAddress;
}
