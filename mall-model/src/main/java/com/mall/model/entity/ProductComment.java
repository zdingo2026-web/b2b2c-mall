package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评价表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductComment extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 订单明细ID */
    private Long orderItemId;

    /** SPU ID */
    private Long spuId;

    /** 会员ID */
    private Long memberId;

    /** 评分: 1-5 */
    private Integer score;

    /** 评价内容 */
    private String content;

    /** 评价图片(逗号分隔) */
    private String images;

    /** 是否匿名: 0否 1是 */
    private Integer isAnonymous;

    /** 回复内容 */
    private String replyContent;

    /** 回复时间 */
    private java.time.LocalDateTime replyTime;

    /** 状态: 0待审核 1已显示 2已隐藏 */
    private Integer status;

    /** 点赞数 */
    @TableField("like_count")
    private Integer likeCount;

    /** 评价标签JSON数组 */
    @TableField("comment_tags")
    private String commentTags;
}
