package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 评价摘要VO
 */
@Data
public class CommentSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论总数 */
    private Integer totalCount;

    /** 好评率 */
    private Double goodRate;

    /** 标签统计 */
    private List<TagCountVO> tags;

    @Data
    public static class TagCountVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private String tagName;

        private Integer count;
    }
}
