package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberCollect extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 会员ID */
    private Long memberId;

    /** 商品SPU ID */
    private Long spuId;

    /** 收藏类型: 1-商品 2-店铺 3-内容 */
    private Integer collectType;

    /** 收藏后是否降价: 0-否 1-是 */
    @TableField("price_decreased")
    private Integer priceDecreased;
}
