package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Banner保存 DTO
 */
@Data
public class BannerSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 图片地址 */
    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    /** 链接地址 */
    private String linkUrl;

    /** 链接类型: 1-商品 2-分类 3-页面 4-外链 */
    @NotNull(message = "链接类型不能为空")
    private Integer linkType;

    /** 排序 */
    private Integer sort;

    /** 开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 结束时间 */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;
}
