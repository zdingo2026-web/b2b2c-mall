package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入驻申请表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantApply extends BaseEntity {

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

    /** 店铺地址 */
    private String address;

    /** 申请状态: 0待审核 1已通过 2已拒绝 */
    private Integer applyStatus;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private java.time.LocalDateTime auditTime;

    /** 审核人ID */
    private Long auditUserId;

    /** 生成的商户ID(审核通过后) */
    private Long tenantId;
}
