package com.mall.service.product;

import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.ProductSkuMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.model.entity.ProductSku;
import com.mall.model.entity.ProductSpu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductSkuMapper productSkuMapper;

    @Mock
    private ProductSpuMapper productSpuMapper;

    @InjectMocks
    private StockService stockService;

    private ProductSku sku;

    @BeforeEach
    void setUp() {
        sku = new ProductSku();
        sku.setId(1L);
        sku.setSpuId(10L);
        sku.setStock(100);
        sku.setPrice(BigDecimal.valueOf(99.9));
    }

    @Nested
    @DisplayName("deductStock")
    class DeductStock {

        @Test
        @DisplayName("Deducts stock successfully when stock is sufficient")
        void deductStock_success() {
            when(productSkuMapper.selectById(1L)).thenReturn(sku);
            when(productSkuMapper.deductStock(1L, 10)).thenReturn(1);
            when(productSpuMapper.deductTotalStock(10L, 10)).thenReturn(1);

            stockService.deductStock(1L, 10);

            verify(productSkuMapper).deductStock(1L, 10);
            verify(productSpuMapper).deductTotalStock(10L, 10);
        }

        @Test
        @DisplayName("Throws SKU_NOT_FOUND when SKU does not exist")
        void deductStock_skuNotFound() {
            when(productSkuMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> stockService.deductStock(999L, 1))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.SKU_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Throws STOCK_NOT_ENOUGH when stock is insufficient")
        void deductStock_insufficientStock() {
            sku.setStock(5);
            when(productSkuMapper.selectById(1L)).thenReturn(sku);

            assertThatThrownBy(() -> stockService.deductStock(1L, 10))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.STOCK_NOT_ENOUGH.getCode());
        }

        @Test
        @DisplayName("Throws STOCK_NOT_ENOUGH when concurrent deduction causes row update to return 0")
        void deductStock_concurrentConflict() {
            sku.setStock(100);
            when(productSkuMapper.selectById(1L)).thenReturn(sku);
            when(productSkuMapper.deductStock(1L, 10)).thenReturn(0);

            assertThatThrownBy(() -> stockService.deductStock(1L, 10))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.STOCK_NOT_ENOUGH.getCode());
        }

        @Test
        @DisplayName("Does not update SPU stock when SKU deduction fails")
        void deductStock_noSpuUpdateOnFailure() {
            sku.setStock(5);
            when(productSkuMapper.selectById(1L)).thenReturn(sku);

            try {
                stockService.deductStock(1L, 10);
            } catch (BusinessException ignored) {
            }

            verify(productSpuMapper, never()).deductTotalStock(anyLong(), anyInt());
        }
    }

    @Nested
    @DisplayName("addStock")
    class AddStock {

        @Test
        @DisplayName("Adds stock back successfully")
        void addStock_success() {
            when(productSkuMapper.selectById(1L)).thenReturn(sku);
            when(productSkuMapper.addStock(1L, 5)).thenReturn(1);
            when(productSpuMapper.addTotalStock(10L, 5)).thenReturn(1);

            stockService.addStock(1L, 5);

            verify(productSkuMapper).addStock(1L, 5);
            verify(productSpuMapper).addTotalStock(10L, 5);
        }

        @Test
        @DisplayName("Throws SKU_NOT_FOUND when SKU does not exist")
        void addStock_skuNotFound() {
            when(productSkuMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> stockService.addStock(999L, 1))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.SKU_NOT_FOUND.getCode());
        }
    }

    @Nested
    @DisplayName("getStock")
    class GetStock {

        @Test
        @DisplayName("Returns current stock for existing SKU")
        void getStock_success() {
            when(productSkuMapper.selectById(1L)).thenReturn(sku);

            Integer stock = stockService.getStock(1L);

            assertThat(stock).isEqualTo(100);
        }

        @Test
        @DisplayName("Throws SKU_NOT_FOUND for non-existent SKU")
        void getStock_skuNotFound() {
            when(productSkuMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> stockService.getStock(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.SKU_NOT_FOUND.getCode());
        }
    }
}
