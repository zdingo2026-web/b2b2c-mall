package com.mall.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.enums.ProductStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.common.util.SnowflakeIdUtil;
import com.mall.dao.mapper.*;
import com.mall.model.dto.OrderCreateDTO;
import com.mall.model.dto.OrderQueryDTO;
import com.mall.model.entity.*;
import com.mall.model.vo.OrderMainVO;
import com.mall.model.vo.OrderLogisticsVO;
import com.mall.service.order.OrderStateMachine.OrderEvent;
import com.mall.service.product.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Order service.
 * Handles order creation (with cart-based or direct purchase), cancellation,
 * order list/detail, and confirm receipt.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderAddressMapper orderAddressMapper;
    private final OrderLogMapper orderLogMapper;
    private final CartService cartService;
    private final StockService stockService;
    private final OrderStateMachine orderStateMachine;
    private final MemberAddressMapper memberAddressMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductSpuMapper productSpuMapper;

    /**
     * Create order from cart or direct items.
     * Steps: validate cart items, split by merchant, calculate freight,
     * deduct stock, generate order snapshot, clear purchased cart items.
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderMainVO createOrder(Long memberId, OrderCreateDTO dto) {
        // Collect order items: from cart (checked) or from direct items
        List<CartItem> cartItems;
        List<Long> cartItemIdsToClear = new ArrayList<>();

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            // Direct purchase: build virtual cart items
            cartItems = new ArrayList<>();
            for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
                ProductSku sku = productSkuMapper.selectById(item.getSkuId());
                if (sku == null) {
                    throw new BusinessException(ResultCode.SKU_NOT_FOUND);
                }
                ProductSpu spu = productSpuMapper.selectById(sku.getSpuId());
                if (spu == null) {
                    throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
                }
                if (spu.getStatus() != ProductStatusEnum.ON_SHELF.getCode()) {
                    throw new BusinessException("商品已下架");
                }

                CartItem ci = new CartItem();
                ci.setTenantId(spu.getTenantId());
                ci.setMemberId(memberId);
                ci.setSpuId(spu.getId());
                ci.setSkuId(sku.getId());
                ci.setProductName(spu.getProductName());
                ci.setSpecValues(sku.getSpecValues());
                ci.setProductImage(sku.getImage() != null ? sku.getImage() : spu.getMainImage());
                ci.setPrice(sku.getPrice());
                ci.setQuantity(item.getQuantity());
                ci.setIsChecked(1);
                cartItems.add(ci);
            }
        } else {
            // Cart-based order
            cartItems = cartService.getCheckedItems(memberId);
            if (cartItems.isEmpty()) {
                throw new BusinessException("请选择要购买的商品");
            }
            cartItemIdsToClear = cartItems.stream()
                    .map(CartItem::getId)
                    .collect(Collectors.toList());
        }

        // Validate product on-sale status for all items
        for (CartItem item : cartItems) {
            ProductSpu spu = productSpuMapper.selectById(item.getSpuId());
            if (spu != null && spu.getStatus() != ProductStatusEnum.ON_SHELF.getCode()) {
                throw new BusinessException("商品已下架");
            }
        }

        // Validate stock for all items
        for (CartItem item : cartItems) {
            stockService.deductStock(item.getSkuId(), item.getQuantity());
        }

        // Get delivery address
        MemberAddress address = memberAddressMapper.selectById(dto.getAddressId());
        if (address == null) {
            throw new BusinessException("收货地址不存在");
        }

        // Group items by tenant (merchant) for order splitting
        Map<Long, List<CartItem>> groupedByTenant = cartItems.stream()
                .collect(Collectors.groupingBy(ci -> ci.getTenantId() != null ? ci.getTenantId() : 0L));

        // Create parent order number
        String parentOrderNo = generateOrderNo();

        // Create sub-order for each tenant
        List<OrderMain> createdOrders = new ArrayList<>();
        for (Map.Entry<Long, List<CartItem>> entry : groupedByTenant.entrySet()) {
            Long tenantId = entry.getKey();
            List<CartItem> tenantItems = entry.getValue();

            // Calculate total amount
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem item : tenantItems) {
                totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            // Calculate freight (MVP: fixed at 0)
            BigDecimal freightAmount = BigDecimal.ZERO;

            // Create order
            OrderMain order = new OrderMain();
            order.setOrderNo(parentOrderNo + (groupedByTenant.size() > 1 ? "-" + tenantId : ""));
            order.setMemberId(memberId);
            order.setParentId(0L);
            order.setOrderType(1);
            order.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
            order.setPayStatus(0);
            order.setTotalAmount(totalAmount);
            order.setFreightAmount(freightAmount);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setPayAmount(totalAmount.add(freightAmount));
            order.setDeliveryType(dto.getDeliveryType() != null ? dto.getDeliveryType() : 1);
            order.setRemark(dto.getRemark());
            order.setExpireTime(LocalDateTime.now().plusMinutes(30));
            order.setVersion(1);
            order.setTenantId(tenantId);

            orderMainMapper.insert(order);
            createdOrders.add(order);

            // Create order items
            for (CartItem ci : tenantItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setSpuId(ci.getSpuId());
                orderItem.setSkuId(ci.getSkuId());
                orderItem.setProductName(ci.getProductName());
                orderItem.setSpecValues(ci.getSpecValues());
                orderItem.setProductImage(ci.getProductImage());
                orderItem.setPrice(ci.getPrice());
                orderItem.setQuantity(ci.getQuantity());
                orderItem.setSubtotal(ci.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
                orderItem.setPayAmount(orderItem.getSubtotal());
                orderItem.setTenantId(tenantId);
                orderItemMapper.insert(orderItem);
            }

            // Create order address snapshot
            OrderAddress orderAddress = new OrderAddress();
            orderAddress.setOrderId(order.getId());
            orderAddress.setReceiverName(address.getReceiverName());
            orderAddress.setReceiverPhone(address.getReceiverPhone());
            orderAddress.setProvinceName(address.getProvinceName());
            orderAddress.setCityName(address.getCityName());
            orderAddress.setDistrictName(address.getDistrictName());
            orderAddress.setDetailAddress(address.getDetailAddress());
            orderAddress.setFullAddress(address.getProvinceName() + address.getCityName()
                    + address.getDistrictName() + address.getDetailAddress());
            orderAddressMapper.insert(orderAddress);

            // Create order log
            OrderLog orderLog = orderStateMachine.buildOrderLog(
                    order.getId(), memberId, 1, "用户", OrderEvent.CANCEL, order.getOrderStatus());
            orderLog.setOperationType(1);
            orderLog.setOperationDesc("创建订单");
            orderLogMapper.insert(orderLog);
        }

        // Clear purchased cart items
        if (!cartItemIdsToClear.isEmpty()) {
            cartService.removeCartItems(cartItemIdsToClear);
        }

        // Return the first (or only) order detail
        OrderMain firstOrder = createdOrders.get(0);
        return getOrderDetail(firstOrder.getOrderNo());
    }

    /**
     * Cancel order (release stock).
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long memberId, String orderNo) {
        OrderMain order = orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // State machine transition
        int newStatus = orderStateMachine.fireEvent(order.getOrderStatus(), OrderEvent.CANCEL);
        order.setOrderStatus(newStatus);
        order.setCancelReason("用户取消");
        orderMainMapper.updateById(order);

        // Release stock
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            stockService.addStock(item.getSkuId(), item.getQuantity());
        }

        // Log
        OrderLog orderLog = orderStateMachine.buildOrderLog(
                order.getId(), memberId, 1, "用户", OrderEvent.CANCEL, newStatus);
        orderLogMapper.insert(orderLog);
    }

    /**
     * Get order list (paginated).
     */
    public PageResult<OrderMainVO> getOrderList(Long memberId, Integer status, int page, int limit) {
        Page<OrderMain> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) {
            wrapper.eq(OrderMain::getMemberId, memberId);
        }
        if (status != null) {
            wrapper.eq(OrderMain::getOrderStatus, status);
        }
        wrapper.orderByDesc(OrderMain::getCreateTime);

        Page<OrderMain> result = orderMainMapper.selectPage(pageParam, wrapper);
        List<OrderMainVO> voList = result.getRecords().stream()
                .map(this::toOrderMainVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    /**
     * Get order list by tenant (for merchant).
     */
    public PageResult<OrderMainVO> getOrderListByTenant(Long tenantId, OrderQueryDTO dto) {
        Page<OrderMain> pageParam = new Page<>(dto.getPage(), dto.getLimit());
        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(OrderMain::getTenantId, tenantId);
        }
        if (dto.getOrderNo() != null && !dto.getOrderNo().isEmpty()) {
            wrapper.like(OrderMain::getOrderNo, dto.getOrderNo());
        }
        if (dto.getOrderStatus() != null) {
            wrapper.eq(OrderMain::getOrderStatus, dto.getOrderStatus());
        }
        if (dto.getPayStatus() != null) {
            wrapper.eq(OrderMain::getPayStatus, dto.getPayStatus());
        }
        wrapper.orderByDesc(OrderMain::getCreateTime);

        Page<OrderMain> result = orderMainMapper.selectPage(pageParam, wrapper);
        List<OrderMainVO> voList = result.getRecords().stream()
                .map(this::toOrderMainVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), dto.getPage(), dto.getLimit());
    }

    /**
     * Get order detail by order number.
     */
    public OrderMainVO getOrderDetail(String orderNo) {
        OrderMain order = orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return toOrderMainVO(order);
    }

    public OrderMain getByOrderNo(String orderNo) {
        return orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
    }

    /**
     * Confirm receipt.
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long memberId, Long orderItemId) {
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        if (orderItem == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        OrderMain order = orderMainMapper.selectById(orderItem.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        int newStatus = orderStateMachine.fireEvent(order.getOrderStatus(), OrderEvent.CONFIRM);
        order.setOrderStatus(newStatus);
        order.setReceiveTime(LocalDateTime.now());
        orderMainMapper.updateById(order);

        OrderLog orderLog = orderStateMachine.buildOrderLog(
                order.getId(), memberId, 1, "用户", OrderEvent.CONFIRM, newStatus);
        orderLogMapper.insert(orderLog);
    }

    /**
     * Ship order (merchant action).
     */
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderItemId, String logisticsCompany, String logisticsNo) {
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        if (orderItem == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        OrderMain order = orderMainMapper.selectById(orderItem.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        int newStatus = orderStateMachine.fireEvent(order.getOrderStatus(), OrderEvent.SHIP);
        order.setOrderStatus(newStatus);
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        order.setDeliveryTime(LocalDateTime.now());
        orderMainMapper.updateById(order);

        OrderLog orderLog = orderStateMachine.buildOrderLog(
                order.getId(), UserContextHelper.getUserId(), 2, "商户", OrderEvent.SHIP, newStatus);
        orderLogMapper.insert(orderLog);
    }

    /**
     * Ship order by order number (platform action).
     */
    @Transactional(rollbackFor = Exception.class)
    public void shipOrderByOrderNo(String orderNo, String logisticsCompany, String logisticsNo) {
        OrderMain order = orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        int newStatus = orderStateMachine.fireEvent(order.getOrderStatus(), OrderEvent.SHIP);
        order.setOrderStatus(newStatus);
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        order.setDeliveryTime(LocalDateTime.now());
        orderMainMapper.updateById(order);

        OrderLog orderLog = orderStateMachine.buildOrderLog(
                order.getId(), UserContextHelper.getUserId(), 1, "平台", OrderEvent.SHIP, newStatus);
        orderLogMapper.insert(orderLog);
    }

    /**
     * Cancel order by merchant (for unpaid orders).
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByMerchant(String orderNo) {
        OrderMain order = orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        int newStatus = orderStateMachine.fireEvent(order.getOrderStatus(), OrderEvent.CANCEL);
        order.setOrderStatus(newStatus);
        order.setCancelReason("商户取消");
        orderMainMapper.updateById(order);

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            stockService.addStock(item.getSkuId(), item.getQuantity());
        }

        OrderLog orderLog = orderStateMachine.buildOrderLog(
                order.getId(), UserContextHelper.getUserId(), 2, "商户", OrderEvent.CANCEL, newStatus);
        orderLogMapper.insert(orderLog);
    }

    /**
     * Cancel order by platform.
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByPlatform(String orderNo) {
        OrderMain order = orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        int newStatus = orderStateMachine.fireEvent(order.getOrderStatus(), OrderEvent.CANCEL);
        order.setOrderStatus(newStatus);
        order.setCancelReason("平台取消");
        orderMainMapper.updateById(order);

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            stockService.addStock(item.getSkuId(), item.getQuantity());
        }

        OrderLog orderLog = orderStateMachine.buildOrderLog(
                order.getId(), UserContextHelper.getUserId(), 1, "平台", OrderEvent.CANCEL, newStatus);
        orderLogMapper.insert(orderLog);
    }

    private String generateOrderNo() {
        return String.valueOf(SnowflakeIdUtil.getInstance().nextId());
    }

    private OrderMainVO toOrderMainVO(OrderMain order) {
        OrderMainVO vo = new OrderMainVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setMemberId(order.getMemberId());
        vo.setParentId(order.getParentId());
        vo.setOrderType(order.getOrderType());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setPayType(order.getPayType());
        vo.setPayTime(order.getPayTime());
        vo.setDeliveryType(order.getDeliveryType());
        vo.setLogisticsCompany(order.getLogisticsCompany());
        vo.setLogisticsNo(order.getLogisticsNo());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setReceiveTime(order.getReceiveTime());
        vo.setRemark(order.getRemark());
        vo.setExpireTime(order.getExpireTime());
        vo.setTenantId(order.getTenantId());
        vo.setTenantName(order.getTenantName());
        vo.setIsReviewed(order.getIsReviewed());
        vo.setInvoiceType(order.getInvoiceType());
        vo.setInvoiceTitle(order.getInvoiceTitle());
        vo.setDeliveryNo(order.getDeliveryNo());
        vo.setDeliveryCompany(order.getDeliveryCompany());
        vo.setDeliveryStatus(order.getDeliveryStatus());
        vo.setCreateTime(order.getCreateTime());

        // Order items
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        List<OrderMainVO.OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderMainVO.OrderItemVO itemVO = new OrderMainVO.OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setSpuId(item.getSpuId());
            itemVO.setSkuId(item.getSkuId());
            itemVO.setProductName(item.getProductName());
            itemVO.setSpecValues(item.getSpecValues());
            itemVO.setProductImage(item.getProductImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSubtotal(item.getSubtotal());
            itemVO.setPayAmount(item.getPayAmount());
            return itemVO;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);

        // Order address
        OrderAddress addr = orderAddressMapper.selectOne(
                new LambdaQueryWrapper<OrderAddress>().eq(OrderAddress::getOrderId, order.getId()));
        if (addr != null) {
            OrderMainVO.OrderAddressVO addrVO = new OrderMainVO.OrderAddressVO();
            addrVO.setReceiverName(addr.getReceiverName());
            addrVO.setReceiverPhone(addr.getReceiverPhone());
            addrVO.setFullAddress(addr.getFullAddress());
            vo.setAddress(addrVO);
        }

        return vo;
    }

    /**
     * Get logistics info for an order.
     */
    public OrderLogisticsVO getLogistics(String orderNo) {
        OrderMain order = orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        OrderLogisticsVO vo = new OrderLogisticsVO();
        vo.setDeliveryNo(order.getDeliveryNo());
        vo.setDeliveryCompany(order.getDeliveryCompany());
        vo.setDeliveryStatus(order.getDeliveryStatus());

        int ds = order.getDeliveryStatus() != null ? order.getDeliveryStatus() : 0;
        String statusText;
        switch (ds) {
            case 1: statusText = "已发货"; break;
            case 2: statusText = "运输中"; break;
            case 3: statusText = "已签收"; break;
            default: statusText = "暂无物流信息"; break;
        }
        vo.setDeliveryStatusText(statusText);

        // Simplified trace data (in production, integrate with real logistics API)
        List<OrderLogisticsVO.LogisticsTrace> traces = new ArrayList<>();
        if (order.getDeliveryStatus() != null && order.getDeliveryStatus() > 0) {
            OrderLogisticsVO.LogisticsTrace t1 = new OrderLogisticsVO.LogisticsTrace();
            t1.setTime(order.getDeliveryTime() != null ? order.getDeliveryTime().toString() : "");
            t1.setDescription("商家已发货");
            traces.add(t1);
        }
        if (order.getDeliveryStatus() != null && order.getDeliveryStatus() >= 3 && order.getReceiveTime() != null) {
            OrderLogisticsVO.LogisticsTrace t2 = new OrderLogisticsVO.LogisticsTrace();
            t2.setTime(order.getReceiveTime().toString());
            t2.setDescription("已签收");
            traces.add(t2);
        }
        vo.setTraces(traces);

        return vo;
    }

    /**
     * Helper class to get user ID from context (avoids direct import of UserContext
     * which may not be set in all contexts).
     */
    private static class UserContextHelper {
        static Long getUserId() {
            return com.mall.common.util.UserContext.getUserId();
        }
    }
}
