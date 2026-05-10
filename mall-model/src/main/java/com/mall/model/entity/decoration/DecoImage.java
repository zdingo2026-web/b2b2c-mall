package com.mall.model.entity.decoration;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("deco_image")
public class DecoImage extends TenantEntity {

    private static final long serialVersionUID = 1L;

    @TableField("album_id")
    private Long albumId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("file_size")
    private Integer fileSize;

    @TableField("file_type")
    private String fileType;

    @TableField("original_name")
    private String originalName;
}
