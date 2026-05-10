package com.mall.model.dto.promotion;

import lombok.Data;

import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class NewcomerPackDTO {

    private Boolean enabled;

    @Length(max = 100)
    private String packName;

    @Length(max = 500)
    private String packDesc;

    @NotEmpty
    private List<Long> couponIds;
}
