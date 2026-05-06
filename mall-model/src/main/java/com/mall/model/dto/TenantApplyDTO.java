package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 入驻申请DTO
 */
@Data
public class TenantApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "商户名称不能为空")
    private String tenantName;

    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    private String contactEmail;

    @NotBlank(message = "营业执照号不能为空")
    private String businessLicense;

    private String licenseImage;

    private String address;
}
