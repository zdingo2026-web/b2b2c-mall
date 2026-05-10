package com.mall.model.dto.decoration;

import lombok.Data;

import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DecoPageSaveDTO {

    @NotNull
    private Integer pageType;

    @NotBlank
    @Length(max = 100)
    private String pageName;

    @NotBlank
    private String componentList;
}
