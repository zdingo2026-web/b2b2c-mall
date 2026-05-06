package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 商户端更新店铺信息参数
 */
@Data
public class ShopUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 50, message = "店铺名称不能超过50字")
    private String tenantName;

    private String logo;

    @Size(max = 500, message = "描述不能超过500字")
    private String description;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String address;
}
