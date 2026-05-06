package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信验证码记录表
 */
@Data
@TableName("sms_code")
public class SmsCode implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String phone;

    private String code;

    /** 业务类型: 1-登录 2-注册 3-修改密码 4-绑定手机 */
    private Integer bizType;

    private String ip;

    private LocalDateTime expireAt;

    /** 是否已验证: 0-未验证 1-已验证 */
    private Integer verified;

    /** 验证尝试次数 */
    private Integer verifyAttempts;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
