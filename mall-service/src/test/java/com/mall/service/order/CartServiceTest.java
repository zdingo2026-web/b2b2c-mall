package com.mall.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.CartItemMapper;
import com.mall.dao.mapper.ProductSkuMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.dao.mapper.TenantMapper;
import com.mall.model.dto.CartAddDTO;
import com.mall.model.entity.CartItem;
import com.mall.model.entity.ProductSku;
import com.mall.model.entity.ProductSpu;
import com.mall.model.entity.Tenant;
import com.mall.model.vo.CartGroupVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductSkuMapper productSkuMapper;

    @Mock
    private ProductSpuMapper productSpuMapper;

    @Mock
    private TenantMapper tenantMapper;

    @InjectMocks
    private CartService cartService;

    private static final Long MEMBER_ID = 1L;
    private static final Long SKU_ID = 100L;
    private static final Long SPU_ID = 10L;
    private static final Long TENANT_ID = 5L;

    private ProductSku sku;
    private ProductSpu spu;

    @BeforeEach
    void setUp() {
        sku = new ProductSku();
        sku.setId(SKU_ID);
        sku.setSpuId(SPU_ID);
        sku.setPrice(BigDecimal.valueOf(99.9));
        sku.setSpecValues("颜色:红色;尺码:XL");
        sku.setImage("img.jpg");
        sku.setStock(50);
        sku.setStatus(1);

        spu = new ProductSpu();
        spu.setId(SPU_ID);
        spu.setTenantId(TENANT_ID);
        spu.setProductName("测试商品");
        spu.setMainImage("main.jpg");
    }

    @Nested
    @DisplayName("addToCart")
    class AddToCart {

        @Test
        @DisplayName("Adds new cart item when SKU not in cart")
        void addToCart_newItem() {
            CartAddDTO dto = new CartAddDTO();
            dto.setSkuId(SKU_ID);
            dto.setQuantity(2);

            when(productSkuMapper.selectById(SKU_ID)).thenReturn(sku);
            when(productSpuMapper.selectById(SPU_ID)).thenReturn(spu);
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(cartItemMapper.insert(any(CartItem.class))).thenReturn(1);

            cartService.addToCart(MEMBER_ID, dto);

            verify(cartItemMapper).insert(any(CartItem.class));
            verify(cartItemMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("Updates quantity when SKU already exists in cart")
        void addToCart_existingItem() {
            CartItem existing = new CartItem();
            existing.setId(1L);
            existing.setQuantity(3);

            CartAddDTO dto = new CartAddDTO();
            dto.setSkuId(SKU_ID);
            dto.setQuantity(2);

            when(productSkuMapper.selectById(SKU_ID)).thenReturn(sku);
            when(productSpuMapper.selectById(SPU_ID)).thenReturn(spu);
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

            cartService.addToCart(MEMBER_ID, dto);

            assertThat(existing.getQuantity()).isEqualTo(5);
            verify(cartItemMapper).updateById(existing);
            verify(cartItemMapper, never()).insert(any());
        }

        @Test
        @DisplayName("Throws SKU_NOT_FOUND when SKU does not exist")
        void addToCart_skuNotFound() {
            CartAddDTO dto = new CartAddDTO();
            dto.setSkuId(999L);
            dto.setQuantity(1);

            when(productSkuMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> cartService.addToCart(MEMBER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.SKU_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Throws PRODUCT_NOT_FOUND when SPU does not exist")
        void addToCart_spuNotFound() {
            CartAddDTO dto = new CartAddDTO();
            dto.setSkuId(SKU_ID);
            dto.setQuantity(1);

            when(productSkuMapper.selectById(SKU_ID)).thenReturn(sku);
            when(productSpuMapper.selectById(SPU_ID)).thenReturn(null);

            assertThatThrownBy(() -> cartService.addToCart(MEMBER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.PRODUCT_NOT_FOUND.getCode());
        }
    }

    @Nested
    @DisplayName("getCartList")
    class GetCartList {

        @Test
        @DisplayName("Returns empty list when cart is empty")
        void getCartList_empty() {
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            List<CartGroupVO> result = cartService.getCartList(MEMBER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Groups cart items by tenant")
        void getCartList_groupByTenant() {
            CartItem item1 = new CartItem();
            item1.setId(1L);
            item1.setTenantId(TENANT_ID);
            item1.setSkuId(SKU_ID);
            item1.setProductName("商品A");
            item1.setPrice(BigDecimal.valueOf(50));
            item1.setQuantity(2);

            CartItem item2 = new CartItem();
            item2.setId(2L);
            item2.setTenantId(999L);
            item2.setSkuId(200L);
            item2.setProductName("商品B");
            item2.setPrice(BigDecimal.valueOf(30));
            item2.setQuantity(1);

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(item1, item2));
            when(productSkuMapper.selectById(anyLong())).thenReturn(sku);

            Tenant tenant = new Tenant();
            tenant.setTenantName("测试商户");
            when(tenantMapper.selectById(TENANT_ID)).thenReturn(tenant);

            List<CartGroupVO> result = cartService.getCartList(MEMBER_ID);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("updateCartItem")
    class UpdateCartItem {

        @Test
        @DisplayName("Updates quantity for positive value")
        void updateCartItem_updateQuantity() {
            CartItem cartItem = new CartItem();
            cartItem.setId(1L);
            cartItem.setMemberId(MEMBER_ID);
            cartItem.setQuantity(3);

            when(cartItemMapper.selectById(1L)).thenReturn(cartItem);
            when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

            cartService.updateCartItem(MEMBER_ID, 1L, 5);

            assertThat(cartItem.getQuantity()).isEqualTo(5);
            verify(cartItemMapper).updateById(cartItem);
        }

        @Test
        @DisplayName("Deletes cart item when quantity is 0 or negative")
        void updateCartItem_deleteWhenZero() {
            CartItem cartItem = new CartItem();
            cartItem.setId(1L);
            cartItem.setMemberId(MEMBER_ID);
            cartItem.setQuantity(3);

            when(cartItemMapper.selectById(1L)).thenReturn(cartItem);
            when(cartItemMapper.deleteById(1L)).thenReturn(1);

            cartService.updateCartItem(MEMBER_ID, 1L, 0);

            verify(cartItemMapper).deleteById(1L);
            verify(cartItemMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("Throws DATA_NOT_FOUND when cart item does not exist")
        void updateCartItem_notFound() {
            when(cartItemMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> cartService.updateCartItem(MEMBER_ID, 999L, 1))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.DATA_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Throws when member does not own the cart item (IDOR)")
        void updateCartItem_idorBlocked() {
            CartItem cartItem = new CartItem();
            cartItem.setId(1L);
            cartItem.setMemberId(999L);
            cartItem.setQuantity(3);

            when(cartItemMapper.selectById(1L)).thenReturn(cartItem);

            assertThatThrownBy(() -> cartService.updateCartItem(MEMBER_ID, 1L, 5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权操作");
        }
    }

    @Nested
    @DisplayName("deleteCartItem")
    class DeleteCartItem {

        @Test
        @DisplayName("Deletes cart item successfully")
        void deleteCartItem_success() {
            CartItem cartItem = new CartItem();
            cartItem.setId(1L);
            cartItem.setMemberId(MEMBER_ID);

            when(cartItemMapper.selectById(1L)).thenReturn(cartItem);
            when(cartItemMapper.deleteById(1L)).thenReturn(1);

            cartService.deleteCartItem(MEMBER_ID, 1L);

            verify(cartItemMapper).deleteById(1L);
        }

        @Test
        @DisplayName("Throws DATA_NOT_FOUND when cart item does not exist")
        void deleteCartItem_notFound() {
            when(cartItemMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> cartService.deleteCartItem(MEMBER_ID, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.DATA_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Throws when member does not own the cart item (IDOR)")
        void deleteCartItem_idorBlocked() {
            CartItem cartItem = new CartItem();
            cartItem.setId(1L);
            cartItem.setMemberId(999L);

            when(cartItemMapper.selectById(1L)).thenReturn(cartItem);

            assertThatThrownBy(() -> cartService.deleteCartItem(MEMBER_ID, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权操作");
        }
    }

    @Nested
    @DisplayName("getCheckedItems")
    class GetCheckedItems {

        @Test
        @DisplayName("Returns only checked cart items")
        void getCheckedItems_returnsChecked() {
            CartItem checked = new CartItem();
            checked.setId(1L);
            checked.setIsChecked(1);

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(checked));

            List<CartItem> result = cartService.getCheckedItems(MEMBER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsChecked()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("removeCartItems")
    class RemoveCartItems {

        @Test
        @DisplayName("Removes cart items by IDs")
        void removeCartItems_success() {
            List<Long> ids = Arrays.asList(1L, 2L, 3L);
            when(cartItemMapper.deleteBatchIds(ids)).thenReturn(3);

            cartService.removeCartItems(ids);

            verify(cartItemMapper).deleteBatchIds(ids);
        }

        @Test
        @DisplayName("Does nothing when list is empty")
        void removeCartItems_emptyList() {
            cartService.removeCartItems(Collections.emptyList());

            verify(cartItemMapper, never()).deleteBatchIds(anyList());
        }

        @Test
        @DisplayName("Does nothing when list is null")
        void removeCartItems_nullList() {
            cartService.removeCartItems(null);

            verify(cartItemMapper, never()).deleteBatchIds(anyList());
        }
    }
}
