package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息VO
 */
@Data
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String content;

    private Integer msgType;

    private Integer isRead;

    private LocalDateTime createTime;
}
