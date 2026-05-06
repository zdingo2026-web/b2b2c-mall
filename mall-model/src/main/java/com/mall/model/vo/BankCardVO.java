package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 银行卡展示VO
 */
@Data
public class BankCardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String bankName;

    private String bankCode;

    private String cardNoMask;

    /** 卡类型: 1-借记卡 2-信用卡 */
    private Integer cardType;

    private String cardColor;

    private String bankLogo;

    private String expiryDate;

    private Integer isDefault;
}
