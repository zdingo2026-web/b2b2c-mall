package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** 手机号 */
    private String phone;

    /** 密码(BCrypt) */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 性别: 0未知 1男 2女 */
    private Integer gender;

    /** 邮箱 */
    private String email;

    /** 余额 */
    private java.math.BigDecimal balance;

    /** 状态: 0禁用 1正常 */
    private Integer status;

    /** 最后登录时间 */
    @TableField("last_login_time")
    private java.time.LocalDateTime lastLoginTime;

    /** 会员等级 1-10 */
    @TableField("member_level")
    private Integer memberLevel;

    /** 积分余额 */
    @TableField("points")
    private Integer points;

    /** 红包余额 */
    @TableField("red_packet_balance")
    private java.math.BigDecimal redPacketBalance;

    /** 优惠券数量(冗余) */
    @TableField("coupon_count")
    private Integer couponCount;

    /** 会员编号 */
    @TableField("member_no")
    private String memberNo;
}
