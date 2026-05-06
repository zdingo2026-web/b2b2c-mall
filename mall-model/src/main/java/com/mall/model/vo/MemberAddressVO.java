package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 收货地址VO
 */
@Data
public class MemberAddressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long memberId;

    private String receiverName;

    private String receiverPhone;

    private Long provinceId;

    private Long cityId;

    private Long districtId;

    private String provinceName;

    private String cityName;

    private String districtName;

    private String detailAddress;

    private Integer isDefault;

    /** 地址标签: 家/公司/学校 */
    private String tag;

    /** 完整地址 */
    private String fullAddress;
}
