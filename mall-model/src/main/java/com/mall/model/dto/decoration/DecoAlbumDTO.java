package com.mall.model.dto.decoration;

import lombok.Data;

import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

@Data
public class DecoAlbumDTO {

    @NotBlank
    @Length(max = 100)
    private String albumName;

    private Integer sortOrder = 0;
}
