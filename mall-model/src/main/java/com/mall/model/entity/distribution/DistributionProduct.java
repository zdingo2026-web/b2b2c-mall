package com.mall.model.entity.distribution;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("distribution_product")
public class DistributionProduct extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("spu_id")
    private Long spuId;

    @TableField("use_global")
    private Integer useGlobal;

    @TableField("rate_level1")
    private BigDecimal rateLevel1;

    @TableField("rate_level2")
    private BigDecimal rateLevel2;

    @TableField("rate_level3")
    private BigDecimal rateLevel3;

    @TableField("can_distribute")
    private Integer canDistribute;
}
