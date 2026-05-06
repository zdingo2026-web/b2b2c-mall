package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CartVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long spuId;

    private Long skuId;

    private String productName;

    private String specValues;

    private String productImage;

    private BigDecimal price;

    private Integer quantity;

    private Integer isChecked;

    private Integer stock;

    private Integer status;
}
