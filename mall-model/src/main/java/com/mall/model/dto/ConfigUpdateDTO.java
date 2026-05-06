package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 配置更新 DTO
 */
@Data
public class ConfigUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @NotNull(message = "配置ID不能为空")
    private Long id;

    /** 配置值 */
    @NotBlank(message = "配置值不能为空")
    private String configValue;
}
