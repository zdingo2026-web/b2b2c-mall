package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Banner VO (C端展示)
 */
@Data
public class BannerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 标题 */
    private String title;

    /** 图片地址 */
    private String imageUrl;

    /** 链接地址 */
    private String linkUrl;

    /** 链接类型: 1-商品 2-分类 3-页面 4-外链 */
    private Integer linkType;
}
