package com.mall.model.vo.decoration;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DecoPageVO {

    private Long id;

    private Long tenantId;

    private Integer pageType;

    private String pageName;

    private String componentList;

    private String draftJson;

    private String publishedJson;

    private Integer isPublished;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
