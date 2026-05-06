package com.mall.service.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.util.TenantContext;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.OrderItemMapper;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.model.entity.OrderItem;
import com.mall.model.entity.OrderMain;
import com.mall.service.product.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mall.service.config.RabbitMQConfig;

import java.util.List;

/**
 * Order timeout consumer.
 * Processes timed-out orders: checks status, cancels order, and restores stock.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final StockService stockService;

    /**
     * Consume order timeout messages.
     * Only cancels orders that are still in PENDING_PAYMENT status.
     *
     * @param orderNo order number
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_TIMEOUT_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderTimeout(String orderNo) {
        log.info("[MQ] Processing order timeout: orderNo={}", orderNo);

        try {
            // Load order first to establish tenant context for proper isolation
            OrderMain order = orderMainMapper.selectOne(
                    new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
            if (order == null) {
                log.info("[MQ] Order not found: orderNo={}", orderNo);
                return;
            }

            // Set tenant context from order data for proper tenant isolation
            TenantContext.setTenantId(order.getTenantId());

            // Only cancel if still in PENDING_PAYMENT status
            if (order.getOrderStatus() == null || order.getOrderStatus() != OrderStatusEnum.PENDING_PAYMENT.getCode()) {
                log.info("[MQ] Order not in PENDING_PAYMENT status, skip: orderNo={}", orderNo);
                return;
            }

            // Cancel the order (conditional update to handle race conditions)
            LambdaUpdateWrapper<OrderMain> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(OrderMain::getId, order.getId())
                    .eq(OrderMain::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getCode())
                    .set(OrderMain::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                    .set(OrderMain::getCancelReason, "超时未支付，自动取消");

            int rows = orderMainMapper.update(null, updateWrapper);
            if (rows > 0) {
                log.info("[MQ] Order cancelled due to timeout: orderNo={}", orderNo);

                // Restore stock for each order item
                List<OrderItem> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
                for (OrderItem item : items) {
                    stockService.addStock(item.getSkuId(), item.getQuantity());
                    log.info("[MQ] Stock restored: skuId={}, quantity={}", item.getSkuId(), item.getQuantity());
                }
            } else {
                log.info("[MQ] Order not in PENDING_PAYMENT status, skip: orderNo={}", orderNo);
            }
        } finally {
            // Clear context to prevent ThreadLocal leaks on reused MQ consumer threads
            UserContext.clear();
            TenantContext.clear();
        }
    }
}
