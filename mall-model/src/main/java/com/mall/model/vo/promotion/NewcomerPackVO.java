package com.mall.model.vo.promotion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class NewcomerPackVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String packName;
    private String packDesc;
    private Boolean enabled;
    private List<Long> couponIds;
}
