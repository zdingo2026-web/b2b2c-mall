package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员银行卡
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_bank_card")
public class MemberBankCard extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("bank_name")
    private String bankName;

    @TableField("bank_code")
    private String bankCode;

    @TableField("card_no")
    private String cardNo;

    @TableField("card_no_mask")
    private String cardNoMask;

    /** 卡类型: 1-借记卡 2-信用卡 */
    @TableField("card_type")
    private Integer cardType;

    @TableField("card_color")
    private String cardColor;

    @TableField("bank_logo")
    private String bankLogo;

    @TableField("expiry_date")
    private String expiryDate;

    @TableField("is_default")
    private Integer isDefault;
}
