package com.mall.model.dto.tenant;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TenantFreezeDTO {
    @NotNull private Integer actionType;
    @NotBlank @Length(max = 500) private String reason;
    private Boolean notifyMerchant = false;
    private String unfreezeTime;
}
