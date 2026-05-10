package com.mall.model.entity.decoration;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("deco_album")
public class DecoAlbum extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("album_name")
    private String albumName;

    @TableField("image_count")
    private Integer imageCount;

    @TableField("sort_order")
    private Integer sortOrder;
}
