package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置 VO (管理端按分组展示)
 */
@Data
public class ConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 配置分组 */
    private String configGroup;

    /** 配置键 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置说明 */
    private String configDesc;

    /** 排序 */
    private Integer sort;
}
