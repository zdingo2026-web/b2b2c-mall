package com.mall.model.dto.distribution;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class DistributorApplyDTO {
    @NotBlank @Length(max = 50) private String realName;
    @NotBlank private String phone;
}
