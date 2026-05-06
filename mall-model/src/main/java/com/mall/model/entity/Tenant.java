package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Tenant extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 商户名称 */
    private String tenantName;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 联系邮箱 */
    private String contactEmail;

    /** 营业执照号 */
    private String businessLicense;

    /** 营业执照图片 */
    private String licenseImage;

    /** 商户Logo */
    private String logo;

    /** 商户描述 */
    private String description;

    /** 状态: 0待审核 1已通过 2已拒绝 3已禁用 */
    private Integer status;

    /** 审核备注 */
    private String auditRemark;

    /** 店铺地址 */
    private String address;

    /** 佣金比例(百分比) */
    private java.math.BigDecimal commissionRate;

    /** 品牌认证: 0-否 1-是 */
    @TableField("brand_verified")
    private Integer brandVerified;

    /** 商品评分 */
    @TableField("score_product")
    private java.math.BigDecimal scoreProduct;

    /** 服务评分 */
    @TableField("score_service")
    private java.math.BigDecimal scoreService;

    /** 物流评分 */
    @TableField("score_logistics")
    private java.math.BigDecimal scoreLogistics;
}
