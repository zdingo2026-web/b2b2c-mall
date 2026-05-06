package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String tenantName;

    private List<CartVO> items;

    private BigDecimal subtotal;
}
