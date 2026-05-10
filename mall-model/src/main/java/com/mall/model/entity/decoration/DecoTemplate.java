package com.mall.model.entity.decoration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("deco_template")
public class DecoTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("template_name")
    private String templateName;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("source")
    private Integer source;

    @TableField("page_type")
    private Integer pageType;

    @TableField("thumbnail")
    private String thumbnail;

    @TableField("component_list")
    private String componentList;

    @TableField("component_count")
    private Integer componentCount;

    @TableField("use_count")
    private Integer useCount;
}
