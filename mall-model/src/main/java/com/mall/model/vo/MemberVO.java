package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员信息VO
 */
@Data
public class MemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String phone;

    private String nickname;

    private String avatar;

    private Integer gender;

    private String email;

    private Integer status;

    private LocalDateTime createTime;

    /** 会员等级 */
    private Integer memberLevel;

    /** 积分余额 */
    private Integer points;

    /** 红包余额 */
    private BigDecimal redPacketBalance;

    /** 优惠券数量 */
    private Integer couponCount;

    /** 会员编号 */
    private String memberNo;
}
