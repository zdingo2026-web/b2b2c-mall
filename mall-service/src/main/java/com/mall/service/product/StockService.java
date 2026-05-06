package com.mall.service.product;

import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.ProductSkuMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.model.entity.ProductSku;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stock service.
 * Handles stock deduction (with row-level locking) and stock replenishment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductSkuMapper productSkuMapper;
    private final ProductSpuMapper productSpuMapper;

    /**
     * Deduct stock for a SKU.
     * Uses parameterized SQL with row-level condition: UPDATE stock=stock-n WHERE stock>=n.
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long skuId, int quantity) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }
        if (sku.getStock() < quantity) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        // C-03 fix: Use parameterized SQL instead of string concatenation
        int updated = productSkuMapper.deductStock(skuId, quantity);
        if (updated == 0) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        productSpuMapper.deductTotalStock(sku.getSpuId(), quantity);
    }

    /**
     * Add stock back (for cancelled orders etc.).
     */
    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long skuId, int quantity) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }

        productSkuMapper.addStock(skuId, quantity);
        productSpuMapper.addTotalStock(sku.getSpuId(), quantity);
    }

    /**
     * Get SKU stock.
     */
    public Integer getStock(Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }
        return sku.getStock();
    }
}
