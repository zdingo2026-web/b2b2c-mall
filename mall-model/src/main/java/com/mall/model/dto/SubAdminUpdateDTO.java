package com.mall.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑子管理员参数
 */
@Data
public class SubAdminUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String realName;

    private String phone;

    private String email;
}
