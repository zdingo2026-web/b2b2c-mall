package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 运费模板保存 DTO
 */
@Data
public class FreightTemplateSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模板名称 */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /** 计费方式: 1-按件数 2-按重量 3-按体积 */
    @NotNull(message = "计费方式不能为空")
    private Integer chargeType;

    /** 默认首件/首重/首体积 */
    @NotNull(message = "首件/首重/首体积不能为空")
    private BigDecimal defaultFirstAmount;

    /** 默认首费 */
    @NotNull(message = "首费不能为空")
    private BigDecimal defaultFirstPrice;

    /** 默认续件/续重/续体积 */
    @NotNull(message = "续件/续重/续体积不能为空")
    private BigDecimal defaultContinueAmount;

    /** 默认续费 */
    @NotNull(message = "续费不能为空")
    private BigDecimal defaultContinuePrice;

    /** 满额包邮(0表示不包邮) */
    private BigDecimal freeAmount;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;
}
