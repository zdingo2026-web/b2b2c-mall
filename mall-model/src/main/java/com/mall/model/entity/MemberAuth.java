package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 第三方绑定表(微信等)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberAuth extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 会员ID */
    private Long memberId;

    /** 身份来源类型: 1微信小程序 2微信公众号 3支付宝 */
    private Integer identityType;

    /** 第三方唯一标识(openid/unionid) */
    private String identifier;

    /** 第三方凭证(access_token等) */
    private String credential;

    /** 昵称 */
    private String nickName;

    /** 头像 */
    private String avatarUrl;
}
