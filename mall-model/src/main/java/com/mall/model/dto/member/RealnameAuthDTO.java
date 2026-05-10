package com.mall.model.dto.member;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RealnameAuthDTO {
    @NotBlank @Length(max = 50) private String realName;
    @NotNull private Integer idCardType;
    @NotBlank private String idCardNo;
    @NotBlank private String idCardFront;
    @NotBlank private String idCardBack;
}
