package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 运费模板表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("freight_template")
public class FreightTemplate extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 模板名称 */
    private String templateName;

    /** 计费方式: 1-按件数 2-按重量 3-按体积 */
    private Integer chargeType;

    /** 默认首件/首重/首体积 */
    private BigDecimal defaultFirstAmount;

    /** 默认首费 */
    private BigDecimal defaultFirstPrice;

    /** 默认续件/续重/续体积 */
    private BigDecimal defaultContinueAmount;

    /** 默认续费 */
    private BigDecimal defaultContinuePrice;

    /** 满额包邮(0表示不包邮) */
    private BigDecimal freeAmount;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;
}
