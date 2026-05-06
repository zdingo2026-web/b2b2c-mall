package com.mall.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.base.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFile extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 原始文件名 */
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件大小(字节) */
    @TableField("file_size")
    private Long fileSize;

    /** 文件类型(扩展名) */
    @TableField("file_type")
    private String fileType;

    /** 业务类型 */
    @TableField("biz_type")
    private String bizType;

    /** 业务ID */
    @TableField("biz_id")
    private Long bizId;

    /** 访问URL */
    private String url;
}
