package com.mall.model.entity.points;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_account")
public class PointsAccount extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("balance")
    private Integer balance;

    @TableField("total_earned")
    private Integer totalEarned;

    @TableField("total_spent")
    private Integer totalSpent;
}
