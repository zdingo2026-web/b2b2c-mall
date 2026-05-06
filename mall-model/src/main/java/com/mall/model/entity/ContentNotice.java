package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容公告表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_notice")
public class ContentNotice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标题 */
    @TableField("title")
    private String title;

    /** 公告类型: 1-活动 2-通知 3-物流 */
    @TableField("notice_type")
    private Integer noticeType;

    /** 状态: 0-禁用 1-启用 */
    @TableField("status")
    private Integer status;

    /** 排序 */
    @TableField("sort")
    private Integer sort;
}
