package com.mall.model.entity;

import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户管理员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantAdmin extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** 密码(BCrypt) */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 角色: 1主管理员 2子管理员 */
    private Integer roleType;

    /** 状态: 0禁用 1正常 */
    private Integer status;

    /** 最后登录时间 */
    private java.time.LocalDateTime lastLoginTime;
}
