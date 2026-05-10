package com.mall.model.entity.member;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_paypwd_reset")
public class MemberPaypwdReset extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("reset_reason")
    private String resetReason;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("status")
    private Integer status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("notify_sent")
    private Integer notifySent;
}
