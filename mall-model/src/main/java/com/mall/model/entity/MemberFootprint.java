package com.mall.model.entity;

import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 浏览足迹表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberFootprint extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 会员ID */
    private Long memberId;

    /** 商品SPU ID */
    private Long spuId;
}
