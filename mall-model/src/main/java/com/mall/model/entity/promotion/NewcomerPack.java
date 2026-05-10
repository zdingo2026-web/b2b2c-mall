package com.mall.model.entity.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 新人礼包表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("newcomer_pack")
public class NewcomerPack extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("pack_name")
    private String packName;

    @TableField("pack_desc")
    private String packDesc;

    @TableField("enabled")
    private Integer enabled;

    @TableField("coupon_ids")
    private String couponIds;
}
