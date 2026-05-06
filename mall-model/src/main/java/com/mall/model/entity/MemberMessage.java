package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员消息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_message")
public class MemberMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 会员ID */
    @TableField("member_id")
    private Long memberId;

    /** 消息标题 */
    @TableField("title")
    private String title;

    /** 消息内容 */
    @TableField("content")
    private String content;

    /** 消息类型: 1-系统 2-订单 3-营销 */
    @TableField("msg_type")
    private Integer msgType;

    /** 是否已读: 0-未读 1-已读 */
    @TableField("is_read")
    private Integer isRead;
}
