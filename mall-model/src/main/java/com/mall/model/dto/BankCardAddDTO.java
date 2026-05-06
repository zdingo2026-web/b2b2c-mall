package com.mall.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 添加银行卡参数
 */
@Data
public class BankCardAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "银行名称不能为空")
    private String bankName;

    private String bankCode;

    @NotBlank(message = "卡号不能为空")
    private String cardNo;

    @NotNull(message = "卡类型不能为空")
    private Integer cardType;

    private String cardColor;

    private String bankLogo;

    private String expiryDate;
}
