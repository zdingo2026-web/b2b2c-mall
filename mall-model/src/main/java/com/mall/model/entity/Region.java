package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 地区表(五级: 省/市/区/街道/社区)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("region")
public class Region extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 上级ID(0表示顶级) */
    private Long parentId;

    /** 层级: 1-省 2-市 3-区 4-街道 5-社区 */
    private Integer level;

    /** 地区名称 */
    private String name;

    /** 地区编码 */
    private String code;

    /** 排序 */
    private Integer sort;
}
