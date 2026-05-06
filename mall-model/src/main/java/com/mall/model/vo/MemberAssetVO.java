package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员资产VO
 */
@Data
public class MemberAssetVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 余额 */
    private BigDecimal balance;

    /** 优惠券数量 */
    private Integer couponCount;

    /** 积分 */
    private Integer points;

    /** 红包余额 */
    private BigDecimal redPacketBalance;
}
