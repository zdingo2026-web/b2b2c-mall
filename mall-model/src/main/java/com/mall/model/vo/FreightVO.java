package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 运费计算结果 VO
 */
@Data
public class FreightVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 运费金额 */
    private BigDecimal freightAmount;

    /** 是否包邮 */
    private Boolean freeShipping;

    /** 运费模板名称 */
    private String templateName;

    /** 计费方式描述 */
    private String chargeTypeDesc;
}
