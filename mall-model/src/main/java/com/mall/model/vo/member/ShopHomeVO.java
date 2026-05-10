package com.mall.model.vo.member;

import lombok.Data;

@Data
public class ShopHomeVO {
    private Long tenantId;
    private String tenantName;
    private String logo;
    private String description;
    private Boolean followed;
    private String decoPageJson;
}
