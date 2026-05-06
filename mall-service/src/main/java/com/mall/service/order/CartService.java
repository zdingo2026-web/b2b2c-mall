package com.mall.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.CartItemMapper;
import com.mall.dao.mapper.ProductSkuMapper;
import com.mall.dao.mapper.ProductSpuMapper;
import com.mall.dao.mapper.TenantMapper;
import com.mall.model.entity.CartItem;
import com.mall.model.entity.ProductSku;
import com.mall.model.entity.ProductSpu;
import com.mall.model.entity.Tenant;
import com.mall.model.vo.CartGroupVO;
import com.mall.model.vo.CartVO;
import com.mall.model.dto.CartAddDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shopping cart service.
 * Handles add to cart, list (grouped by merchant), update quantity, delete, check/uncheck.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductSpuMapper productSpuMapper;
    private final TenantMapper tenantMapper;

    /**
     * Add to cart. If same SKU exists, quantity +1.
     */
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long memberId, CartAddDTO dto) {
        ProductSku sku = productSkuMapper.selectById(dto.getSkuId());
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }

        ProductSpu spu = productSpuMapper.selectById(sku.getSpuId());
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // Check if cart item with same SKU already exists
        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getMemberId, memberId)
                        .eq(CartItem::getSkuId, dto.getSkuId()));

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            existing.setPrice(sku.getPrice());
            cartItemMapper.updateById(existing);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setTenantId(spu.getTenantId());
            cartItem.setMemberId(memberId);
            cartItem.setSpuId(spu.getId());
            cartItem.setSkuId(sku.getId());
            cartItem.setProductName(spu.getProductName());
            cartItem.setSpecValues(sku.getSpecValues());
            cartItem.setProductImage(sku.getImage() != null ? sku.getImage() : spu.getMainImage());
            cartItem.setPrice(sku.getPrice());
            cartItem.setQuantity(dto.getQuantity());
            cartItem.setIsChecked(1);
            cartItemMapper.insert(cartItem);
        }
    }

    /**
     * Get cart list grouped by merchant.
     */
    public List<CartGroupVO> getCartList(Long memberId) {
        List<CartItem> cartItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getMemberId, memberId)
                        .orderByDesc(CartItem::getCreateTime));

        if (cartItems.isEmpty()) {
            return new ArrayList<>();
        }

        // Group by tenantId
        Map<Long, List<CartItem>> groupedByTenant = cartItems.stream()
                .collect(Collectors.groupingBy(CartItem::getTenantId));

        List<CartGroupVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<CartItem>> entry : groupedByTenant.entrySet()) {
            CartGroupVO groupVO = new CartGroupVO();
            groupVO.setTenantId(entry.getKey());

            // Get tenant name
            if (entry.getKey() != null && entry.getKey() > 0) {
                Tenant tenant = tenantMapper.selectById(entry.getKey());
                if (tenant != null) {
                    groupVO.setTenantName(tenant.getTenantName());
                }
            } else {
                groupVO.setTenantName("平台自营");
            }

            List<CartVO> items = entry.getValue().stream().map(this::toCartVO).collect(Collectors.toList());
            groupVO.setItems(items);

            // Calculate subtotal
            BigDecimal subtotal = items.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            groupVO.setSubtotal(subtotal);

            result.add(groupVO);
        }

        return result;
    }

    /**
     * Update cart item quantity.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCartItem(Long memberId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (!cartItem.getMemberId().equals(memberId)) {
            throw new BusinessException("无权操作");
        }
        if (quantity <= 0) {
            cartItemMapper.deleteById(cartItemId);
        } else {
            cartItem.setQuantity(quantity);
            cartItemMapper.updateById(cartItem);
        }
    }

    /**
     * Delete cart item.
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (!cartItem.getMemberId().equals(memberId)) {
            throw new BusinessException("无权操作");
        }
        cartItemMapper.deleteById(cartItemId);
    }

    /**
     * Check/uncheck cart items.
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkCartItems(Long memberId, List<Long> cartItemIds, Integer checked) {
        cartItemMapper.update(null,
                new LambdaUpdateWrapper<CartItem>()
                        .eq(CartItem::getMemberId, memberId)
                        .in(CartItem::getId, cartItemIds)
                        .set(CartItem::getIsChecked, checked));
    }

    /**
     * Get checked cart items for order creation.
     */
    public List<CartItem> getCheckedItems(Long memberId) {
        return cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getMemberId, memberId)
                        .eq(CartItem::getIsChecked, 1));
    }

    /**
     * Remove cart items by IDs (after order creation).
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeCartItems(List<Long> cartItemIds) {
        if (cartItemIds != null && !cartItemIds.isEmpty()) {
            cartItemMapper.deleteBatchIds(cartItemIds);
        }
    }

    private CartVO toCartVO(CartItem cartItem) {
        CartVO vo = new CartVO();
        vo.setId(cartItem.getId());
        vo.setSpuId(cartItem.getSpuId());
        vo.setSkuId(cartItem.getSkuId());
        vo.setProductName(cartItem.getProductName());
        vo.setSpecValues(cartItem.getSpecValues());
        vo.setProductImage(cartItem.getProductImage());
        vo.setPrice(cartItem.getPrice());
        vo.setQuantity(cartItem.getQuantity());
        vo.setIsChecked(cartItem.getIsChecked());

        // Get current stock and status
        ProductSku sku = productSkuMapper.selectById(cartItem.getSkuId());
        if (sku != null) {
            vo.setStock(sku.getStock());
            vo.setStatus(sku.getStatus());
        }

        return vo;
    }
}
