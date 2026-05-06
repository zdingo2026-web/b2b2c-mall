package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页楼层配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_floor")
public class ContentFloor extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 楼层名称 */
    private String floorName;

    /** 关联分类ID */
    private Long categoryId;

    /** 展示样式: 1-左右 2-上下 3-网格 */
    private Integer style;

    /** 展示商品数量 */
    private Integer productCount;

    /** 排序(升序) */
    private Integer sort;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;
}
