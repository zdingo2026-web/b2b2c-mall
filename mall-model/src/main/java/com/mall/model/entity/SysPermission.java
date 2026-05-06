package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 父权限ID */
    private Long parentId;

    /** 权限名称 */
    private String permissionName;

    /** 权限类型: 1菜单 2按钮 */
    private Integer permissionType;

    /** 权限标识 */
    private String permissionCode;

    /** 路由路径 */
    private String path;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 状态: 0禁用 1正常 */
    private Integer status;
}
