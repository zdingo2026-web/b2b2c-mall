package com.mall.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.model.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * Payment record Mapper.
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    /**
     * Deduct member balance with row-level lock.
     * Only succeeds when balance >= amount.
     */
    @Update("UPDATE member SET balance = balance - #{amount} WHERE id = #{memberId} AND balance >= #{amount} AND deleted = 0")
    int deductBalance(@Param("memberId") Long memberId, @Param("amount") BigDecimal amount);

    /**
     * Add member balance.
     */
    @Update("UPDATE member SET balance = balance + #{amount} WHERE id = #{memberId} AND deleted = 0")
    int addBalance(@Param("memberId") Long memberId, @Param("amount") BigDecimal amount);
}
