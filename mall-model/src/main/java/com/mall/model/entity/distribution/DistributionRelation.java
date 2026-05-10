package com.mall.model.entity.distribution;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("distribution_relation")
public class DistributionRelation extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("member_id")
    private Long memberId;

    @TableField("parent_level1")
    private Long parentLevel1;

    @TableField("parent_level2")
    private Long parentLevel2;

    @TableField("parent_level3")
    private Long parentLevel3;
}
