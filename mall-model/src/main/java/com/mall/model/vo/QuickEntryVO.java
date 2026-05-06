package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 快捷入口VO
 */
@Data
public class QuickEntryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 入口名称 */
    private String name;

    /** remixicon图标名 */
    private String icon;

    /** 图标背景色 */
    private String bgColor;

    /** 图标颜色 */
    private String iconColor;

    /** 跳转类型: category/flashsale/coupon/groupbuy/points/new/brand/vip/presale/more */
    private String linkType;

    /** 跳转链接(占位) */
    private String linkUrl;
}
