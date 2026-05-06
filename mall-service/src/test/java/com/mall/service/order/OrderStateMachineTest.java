package com.mall.service.order;

import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.model.entity.OrderLog;
import com.mall.service.order.OrderStateMachine.OrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderStateMachineTest {

    private OrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new OrderStateMachine();
        stateMachine.init();
    }

    @Nested
    @DisplayName("Valid state transitions")
    class ValidTransitions {

        @Test
        @DisplayName("PENDING_PAYMENT + PAY -> PENDING_SHIPMENT")
        void pay_transitionsToPendingShipment() {
            int newStatus = stateMachine.fireEvent(
                    OrderStatusEnum.PENDING_PAYMENT.getCode(), OrderEvent.PAY);
            assertThat(newStatus).isEqualTo(OrderStatusEnum.PENDING_SHIPMENT.getCode());
        }

        @Test
        @DisplayName("PENDING_SHIPMENT + SHIP -> PENDING_RECEIPT")
        void ship_transitionsToPendingReceipt() {
            int newStatus = stateMachine.fireEvent(
                    OrderStatusEnum.PENDING_SHIPMENT.getCode(), OrderEvent.SHIP);
            assertThat(newStatus).isEqualTo(OrderStatusEnum.PENDING_RECEIPT.getCode());
        }

        @Test
        @DisplayName("PENDING_RECEIPT + CONFIRM -> COMPLETED")
        void confirm_transitionsToCompleted() {
            int newStatus = stateMachine.fireEvent(
                    OrderStatusEnum.PENDING_RECEIPT.getCode(), OrderEvent.CONFIRM);
            assertThat(newStatus).isEqualTo(OrderStatusEnum.COMPLETED.getCode());
        }

        @Test
        @DisplayName("PENDING_PAYMENT + CANCEL -> CANCELLED")
        void cancel_transitionsToCancelled() {
            int newStatus = stateMachine.fireEvent(
                    OrderStatusEnum.PENDING_PAYMENT.getCode(), OrderEvent.CANCEL);
            assertThat(newStatus).isEqualTo(OrderStatusEnum.CANCELLED.getCode());
        }

        @Test
        @DisplayName("PENDING_SHIPMENT + REFUND_APPLY -> REFUNDING")
        void refundApply_fromPendingShipment() {
            int newStatus = stateMachine.fireEvent(
                    OrderStatusEnum.PENDING_SHIPMENT.getCode(), OrderEvent.REFUND_APPLY);
            assertThat(newStatus).isEqualTo(OrderStatusEnum.REFUNDING.getCode());
        }

        @Test
        @DisplayName("PENDING_RECEIPT + REFUND_APPLY -> REFUNDING")
        void refundApply_fromPendingReceipt() {
            int newStatus = stateMachine.fireEvent(
                    OrderStatusEnum.PENDING_RECEIPT.getCode(), OrderEvent.REFUND_APPLY);
            assertThat(newStatus).isEqualTo(OrderStatusEnum.REFUNDING.getCode());
        }

        @Test
        @DisplayName("COMPLETED + REFUND_APPLY -> REFUNDING")
        void refundApply_fromCompleted() {
            int newStatus = stateMachine.fireEvent(
                    OrderStatusEnum.COMPLETED.getCode(), OrderEvent.REFUND_APPLY);
            assertThat(newStatus).isEqualTo(OrderStatusEnum.REFUNDING.getCode());
        }

        @Test
        @DisplayName("REFUNDING + REFUND_SUCCESS -> REFUNDED")
        void refundSuccess_transitionsToRefunded() {
            int newStatus = stateMachine.fireEvent(
                    OrderStatusEnum.REFUNDING.getCode(), OrderEvent.REFUND_SUCCESS);
            assertThat(newStatus).isEqualTo(OrderStatusEnum.REFUNDED.getCode());
        }
    }

    @Nested
    @DisplayName("Invalid state transitions")
    class InvalidTransitions {

        @Test
        @DisplayName("CANCELLED + PAY throws BusinessException")
        void cancelled_cannotPay() {
            assertThatThrownBy(() -> stateMachine.fireEvent(
                    OrderStatusEnum.CANCELLED.getCode(), OrderEvent.PAY))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.ORDER_STATUS_ERROR.getCode());
        }

        @Test
        @DisplayName("COMPLETED + CONFIRM throws BusinessException")
        void completed_cannotConfirm() {
            assertThatThrownBy(() -> stateMachine.fireEvent(
                    OrderStatusEnum.COMPLETED.getCode(), OrderEvent.CONFIRM))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("PENDING_PAYMENT + SHIP throws BusinessException")
        void pendingPayment_cannotShip() {
            assertThatThrownBy(() -> stateMachine.fireEvent(
                    OrderStatusEnum.PENDING_PAYMENT.getCode(), OrderEvent.SHIP))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("REFUNDED + any event throws BusinessException")
        void refunded_noTransitions() {
            for (OrderEvent event : OrderEvent.values()) {
                assertThatThrownBy(() -> stateMachine.fireEvent(
                        OrderStatusEnum.REFUNDED.getCode(), event))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("CANCELLED + CANCEL throws BusinessException")
        void cancelled_cannotCancelAgain() {
            assertThatThrownBy(() -> stateMachine.fireEvent(
                    OrderStatusEnum.CANCELLED.getCode(), OrderEvent.CANCEL))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("PENDING_SHIPMENT + CANCEL throws BusinessException")
        void pendingShipment_cannotCancel() {
            assertThatThrownBy(() -> stateMachine.fireEvent(
                    OrderStatusEnum.PENDING_SHIPMENT.getCode(), OrderEvent.CANCEL))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("canTransit method")
    class CanTransit {

        @Test
        @DisplayName("Returns true for valid transition")
        void validTransition_returnsTrue() {
            assertThat(stateMachine.canTransit(
                    OrderStatusEnum.PENDING_PAYMENT.getCode(), OrderEvent.PAY)).isTrue();
        }

        @Test
        @DisplayName("Returns false for invalid transition")
        void invalidTransition_returnsFalse() {
            assertThat(stateMachine.canTransit(
                    OrderStatusEnum.CANCELLED.getCode(), OrderEvent.PAY)).isFalse();
        }
    }

    @Nested
    @DisplayName("buildOrderLog method")
    class BuildOrderLog {

        @Test
        @DisplayName("Builds order log with correct fields")
        void buildsLogCorrectly() {
            OrderLog log = stateMachine.buildOrderLog(1L, 100L, 1, "测试用户",
                    OrderEvent.PAY, OrderStatusEnum.PENDING_SHIPMENT.getCode());
            assertThat(log.getOrderId()).isEqualTo(1L);
            assertThat(log.getOperatorId()).isEqualTo(100L);
            assertThat(log.getOrderStatus()).isEqualTo(OrderStatusEnum.PENDING_SHIPMENT.getCode());
        }

        @Test
        @DisplayName("PAY event maps to operation type 2")
        void payOperationType() {
            OrderLog log = stateMachine.buildOrderLog(1L, 1L, 1, "", OrderEvent.PAY, 1);
            assertThat(log.getOperationType()).isEqualTo(2);
        }

        @Test
        @DisplayName("CANCEL event maps to operation type 5")
        void cancelOperationType() {
            OrderLog log = stateMachine.buildOrderLog(1L, 1L, 1, "", OrderEvent.CANCEL, 4);
            assertThat(log.getOperationType()).isEqualTo(5);
        }
    }
}
