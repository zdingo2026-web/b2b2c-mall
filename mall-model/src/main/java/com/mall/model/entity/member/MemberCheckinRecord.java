package com.mall.model.entity.member;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_checkin_record")
public class MemberCheckinRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("checkin_date")
    private LocalDate checkinDate;

    @TableField("continuous_days")
    private Integer continuousDays;

    @TableField("points_earned")
    private Integer pointsEarned;

    @TableField("bonus_earned")
    private Integer bonusEarned;
}
