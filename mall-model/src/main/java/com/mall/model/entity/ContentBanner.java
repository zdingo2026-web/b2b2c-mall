package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 首页轮播图表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_banner")
public class ContentBanner extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 标题 */
    private String title;

    /** 图片地址 */
    private String imageUrl;

    /** 链接地址 */
    private String linkUrl;

    /** 链接类型: 1-商品 2-分类 3-页面 4-外链 */
    private Integer linkType;

    /** 排序(升序) */
    private Integer sort;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;

    /** 展示位置: 1-首页 2-分类页 */
    @TableField("position")
    private Integer position;
}
