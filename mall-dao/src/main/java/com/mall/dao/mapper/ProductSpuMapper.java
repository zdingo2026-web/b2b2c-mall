package com.mall.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.model.entity.ProductSpu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * SPU Mapper
 */
public interface ProductSpuMapper extends BaseMapper<ProductSpu> {

    @Update("UPDATE product_spu SET total_stock = total_stock - #{quantity} WHERE id = #{spuId}")
    int deductTotalStock(@Param("spuId") Long spuId, @Param("quantity") int quantity);

    @Update("UPDATE product_spu SET total_stock = total_stock + #{quantity} WHERE id = #{spuId}")
    int addTotalStock(@Param("spuId") Long spuId, @Param("quantity") int quantity);
}
