package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 微信登录DTO
 */
@Data
public class WechatLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "code不能为空")
    private String code;

    /** 加密数据 */
    private String encryptedData;

    /** 初始向量 */
    private String iv;
}
