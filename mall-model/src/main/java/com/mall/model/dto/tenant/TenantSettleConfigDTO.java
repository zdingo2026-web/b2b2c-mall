package com.mall.model.dto.tenant;

import lombok.Data;

@Data
public class TenantSettleConfigDTO {
    private Boolean enabled;
    private String settleNotice;
    private String settleAgreement;
    private Boolean autoAudit;
}
