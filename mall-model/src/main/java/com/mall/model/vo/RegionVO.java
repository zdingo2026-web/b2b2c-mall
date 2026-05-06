package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 地区树节点 VO
 */
@Data
public class RegionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 地区名称 */
    private String name;

    /** 层级 */
    private Integer level;

    /** 地区编码 */
    private String code;

    /** 子节点 */
    private List<RegionVO> children;
}
