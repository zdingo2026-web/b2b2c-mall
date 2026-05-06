package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 楼层保存 DTO
 */
@Data
public class FloorSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 楼层名称 */
    @NotBlank(message = "楼层名称不能为空")
    private String floorName;

    /** 关联分类ID */
    private Long categoryId;

    /** 展示样式: 1-左右 2-上下 3-网格 */
    @NotNull(message = "展示样式不能为空")
    private Integer style;

    /** 展示商品数量 */
    private Integer productCount;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;
}
