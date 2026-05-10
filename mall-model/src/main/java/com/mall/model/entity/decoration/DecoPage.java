package com.mall.model.entity.decoration;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("deco_page")
public class DecoPage extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("page_type")
    private Integer pageType;

    @TableField("page_name")
    private String pageName;

    @TableField("component_list")
    private String componentList;

    @TableField("draft_json")
    private String draftJson;

    @TableField("published_json")
    private String publishedJson;

    @TableField("is_published")
    private Integer isPublished;

    @TableField("publish_time")
    private LocalDateTime publishTime;
}
