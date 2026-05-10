package com.mall.model.dto.decoration;

import lombok.Data;

import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DecoTemplateSaveDTO {

    @NotBlank
    @Length(max = 100)
    private String templateName;

    @NotNull
    private Integer pageType;

    @NotBlank
    private String componentList;
}
