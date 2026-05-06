package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 平台管理员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAdmin extends BaseEntity {

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

    /** 角色ID */
    private Long roleId;

    /** 状态: 0禁用 1正常 */
    private Integer status;

    /** 最后登录时间 */
    private java.time.LocalDateTime lastLoginTime;
}
