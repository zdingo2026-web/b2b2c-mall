package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 商品评价提交参数
 */
@Data
public class CommentCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** SPU ID */
    @NotNull(message = "商品SPU ID不能为空")
    private Long spuId;

    /** 订单项ID */
    @NotNull(message = "订单项ID不能为空")
    private Long orderItemId;

    /** 评价内容 */
    @NotNull(message = "评价内容不能为空")
    @Size(min = 1, max = 500, message = "评价内容长度1-500字")
    private String content;

    /** 评价图片URL列表 */
    private List<String> images;

    /** 评分: 1-5 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer score;

    /** 是否匿名: 0否 1是 */
    private Integer isAnonymous;
}
