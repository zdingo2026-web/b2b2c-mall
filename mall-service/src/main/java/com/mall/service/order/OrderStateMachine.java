package com.mall.service.order;

import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.model.entity.OrderItem;
import com.mall.model.entity.OrderLog;
import com.mall.model.entity.OrderMain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Order state machine.
 * Manages order status transitions using a Map-based state transition table.
 */
@Slf4j
@Component
public class OrderStateMachine {

    /**
     * Order events.
     */
    public enum OrderEvent {
        PAY,           // Payment received
        SHIP,          // Merchant ships
        CONFIRM,       // Buyer confirms receipt
        CANCEL,        // Cancel order
        REFUND_APPLY,  // Apply for refund
        REFUND_SUCCESS // Refund completed
    }

    /**
     * State transition key: (current status code, event) -> target status code.
     */
    private final Map<StateKey, Integer> transitionMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // PENDING_PAYMENT -> PENDING_SHIPMENT (on PAY)
        transitionMap.put(new StateKey(OrderStatusEnum.PENDING_PAYMENT.getCode(), OrderEvent.PAY),
                OrderStatusEnum.PENDING_SHIPMENT.getCode());

        // PENDING_SHIPMENT -> PENDING_RECEIPT (on SHIP)
        transitionMap.put(new StateKey(OrderStatusEnum.PENDING_SHIPMENT.getCode(), OrderEvent.SHIP),
                OrderStatusEnum.PENDING_RECEIPT.getCode());

        // PENDING_RECEIPT -> COMPLETED (on CONFIRM)
        transitionMap.put(new StateKey(OrderStatusEnum.PENDING_RECEIPT.getCode(), OrderEvent.CONFIRM),
                OrderStatusEnum.COMPLETED.getCode());

        // PENDING_PAYMENT -> CANCELLED (on CANCEL)
        transitionMap.put(new StateKey(OrderStatusEnum.PENDING_PAYMENT.getCode(), OrderEvent.CANCEL),
                OrderStatusEnum.CANCELLED.getCode());

        // PENDING_SHIPMENT -> REFUNDING (on REFUND_APPLY)
        transitionMap.put(new StateKey(OrderStatusEnum.PENDING_SHIPMENT.getCode(), OrderEvent.REFUND_APPLY),
                OrderStatusEnum.REFUNDING.getCode());

        // PENDING_RECEIPT -> REFUNDING (on REFUND_APPLY)
        transitionMap.put(new StateKey(OrderStatusEnum.PENDING_RECEIPT.getCode(), OrderEvent.REFUND_APPLY),
                OrderStatusEnum.REFUNDING.getCode());

        // COMPLETED -> REFUNDING (on REFUND_APPLY)
        transitionMap.put(new StateKey(OrderStatusEnum.COMPLETED.getCode(), OrderEvent.REFUND_APPLY),
                OrderStatusEnum.REFUNDING.getCode());

        // REFUNDING -> REFUNDED (on REFUND_SUCCESS)
        transitionMap.put(new StateKey(OrderStatusEnum.REFUNDING.getCode(), OrderEvent.REFUND_SUCCESS),
                OrderStatusEnum.REFUNDED.getCode());
    }

    /**
     * Fire an event on the order, validate transition, and return the target status.
     *
     * @param currentStatus current order status code
     * @param event         the event to fire
     * @return target status code after transition
     */
    public int fireEvent(int currentStatus, OrderEvent event) {
        StateKey key = new StateKey(currentStatus, event);
        Integer targetStatus = transitionMap.get(key);

        if (targetStatus == null) {
            OrderStatusEnum current = OrderStatusEnum.fromCode(currentStatus);
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR,
                    "订单状态[" + current.getDesc() + "]不允许执行[" + event.name() + "]操作");
        }

        log.info("Order state transition: {} + {} -> {}",
                OrderStatusEnum.fromCode(currentStatus).getDesc(),
                event.name(),
                OrderStatusEnum.fromCode(targetStatus).getDesc());

        return targetStatus;
    }

    /**
     * Check if a transition is valid without executing it.
     */
    public boolean canTransit(int currentStatus, OrderEvent event) {
        return transitionMap.containsKey(new StateKey(currentStatus, event));
    }

    /**
     * Build an order log entry for a state transition.
     */
    public OrderLog buildOrderLog(Long orderId, Long operatorId, Integer operatorType,
                                   String operatorName, OrderEvent event, int newStatus) {
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setOperatorId(operatorId);
        orderLog.setOperatorType(operatorType);
        orderLog.setOperatorName(operatorName);
        orderLog.setOperationType(eventToOperationType(event));
        orderLog.setOperationDesc(eventToDesc(event));
        orderLog.setOrderStatus(newStatus);
        return orderLog;
    }

    private int eventToOperationType(OrderEvent event) {
        switch (event) {
            case PAY: return 2;
            case SHIP: return 3;
            case CONFIRM: return 4;
            case CANCEL: return 5;
            case REFUND_APPLY: return 6;
            case REFUND_SUCCESS: return 6;
            default: return 0;
        }
    }

    private String eventToDesc(OrderEvent event) {
        switch (event) {
            case PAY: return "订单支付";
            case SHIP: return "订单发货";
            case CONFIRM: return "确认收货";
            case CANCEL: return "取消订单";
            case REFUND_APPLY: return "申请退款";
            case REFUND_SUCCESS: return "退款完成";
            default: return event.name();
        }
    }

    /**
     * Composite key for state transition map.
     */
    private static class StateKey {
        final int status;
        final OrderEvent event;

        StateKey(int status, OrderEvent event) {
            this.status = status;
            this.event = event;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StateKey stateKey = (StateKey) o;
            return status == stateKey.status && event == stateKey.event;
        }

        @Override
        public int hashCode() {
            return 31 * status + (event != null ? event.hashCode() : 0);
        }
    }
}
