package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回VO
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 用户信息 */
    private UserInfo userInfo;

    @Data
    public static class UserInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long userId;

        private String username;

        private String nickname;

        private String avatar;

        private Integer userType;

        private Long tenantId;
    }
}
