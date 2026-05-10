package com.mall.model.entity.member;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_realname_auth")
public class MemberRealnameAuth extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("real_name")
    private String realName;

    @TableField("id_card_type")
    private Integer idCardType;

    @TableField("id_card_no")
    private String idCardNo;

    @TableField("id_card_front")
    private String idCardFront;

    @TableField("id_card_back")
    private String idCardBack;

    @TableField("status")
    private Integer status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("auditor_id")
    private Long auditorId;
}
