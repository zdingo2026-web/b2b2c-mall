package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberAddress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 会员ID */
    private Long memberId;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人手机号 */
    private String receiverPhone;

    /** 省ID */
    private Long provinceId;

    /** 市ID */
    private Long cityId;

    /** 区ID */
    private Long districtId;

    /** 省名称 */
    private String provinceName;

    /** 市名称 */
    private String cityName;

    /** 区名称 */
    private String districtName;

    /** 详细地址 */
    private String detailAddress;

    /** 是否默认: 0否 1是 */
    private Integer isDefault;

    /** 地址标签: 家/公司/学校 */
    private String tag;
}
