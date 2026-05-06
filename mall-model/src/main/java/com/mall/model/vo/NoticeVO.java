package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 公告VO
 */
@Data
public class NoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private Integer noticeType;
}
