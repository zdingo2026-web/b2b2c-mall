package com.mall.model.vo.tenant;

import lombok.Data;

@Data
public class TenantSettleConfigVO {
    private Long id;
    private Boolean enabled;
    private String settleNotice;
    private String settleAgreement;
    private Boolean autoAudit;
}
