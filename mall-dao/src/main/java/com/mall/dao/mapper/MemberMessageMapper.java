package com.mall.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.model.entity.MemberMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员消息 Mapper
 */
@Mapper
public interface MemberMessageMapper extends BaseMapper<MemberMessage> {
}
