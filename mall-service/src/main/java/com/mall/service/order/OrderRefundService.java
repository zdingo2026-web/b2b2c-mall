package com.mall.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.common.util.SnowflakeIdUtil;
import com.mall.dao.mapper.OrderItemMapper;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.dao.mapper.OrderRefundMapper;
import com.mall.model.dto.RefundApplyDTO;
import com.mall.model.entity.OrderItem;
import com.mall.model.entity.OrderMain;
import com.mall.model.entity.OrderRefund;
import com.mall.service.order.OrderStateMachine.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order refund service.
 * Handles refund application and merchant audit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderRefundService {

    private final OrderRefundMapper orderRefundMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStateMachine orderStateMachine;

    /**
     * Apply for refund/return.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long applyRefund(Long memberId, RefundApplyDTO dto) {
        OrderMain order = orderMainMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        OrderItem orderItem = orderItemMapper.selectById(dto.getOrderItemId());
        if (orderItem == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // Store the order status before refund (for potential rejection revert)
        int preRefundStatus = order.getOrderStatus();

        // Fire refund apply event through state machine
        int newStatus = orderStateMachine.fireEvent(order.getOrderStatus(), OrderEvent.REFUND_APPLY);
        order.setOrderStatus(newStatus);
        orderMainMapper.updateById(order);

        // Create refund record
        OrderRefund refund = new OrderRefund();
        refund.setRefundNo("RF" + SnowflakeIdUtil.getInstance().nextIdStr());
        refund.setOrderId(dto.getOrderId());
        refund.setOrderItemId(dto.getOrderItemId());
        refund.setMemberId(memberId);
        refund.setTenantId(order.getTenantId());
        refund.setRefundType(dto.getRefundType());
        refund.setRefundReason(dto.getRefundReason());
        refund.setRefundDesc(dto.getRefundDesc());
        refund.setRefundImages(dto.getRefundImages());
        refund.setRefundAmount(dto.getRefundAmount() != null ? dto.getRefundAmount() : orderItem.getPayAmount());
        refund.setRefundStatus(0);
        refund.setPreRefundStatus(preRefundStatus);
        orderRefundMapper.insert(refund);

        return refund.getId();
    }

    /**
     * Merchant handles refund (approve or reject).
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleRefund(Long refundId, boolean pass, String reason) {
        OrderRefund refund = orderRefundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (refund.getRefundStatus() != 0) {
            throw new BusinessException("该退款申请已处理");
        }

        refund.setAuditRemark(reason);
        refund.setAuditTime(LocalDateTime.now());

        if (pass) {
            refund.setRefundStatus(1);
            refund.setRefundTime(LocalDateTime.now());

            // Update order status via state machine
            OrderMain order = orderMainMapper.selectById(refund.getOrderId());
            if (order != null && orderStateMachine.canTransit(order.getOrderStatus(), OrderEvent.REFUND_SUCCESS)) {
                int newStatus = orderStateMachine.fireEvent(order.getOrderStatus(), OrderEvent.REFUND_SUCCESS);
                order.setOrderStatus(newStatus);
                orderMainMapper.updateById(order);
            }
        } else {
            refund.setRefundStatus(2);

            // If rejected, revert order status to the pre-refund status
            OrderMain order = orderMainMapper.selectById(refund.getOrderId());
            if (order != null && order.getOrderStatus() == OrderStatusEnum.REFUNDING.getCode()) {
                int restoreStatus = refund.getPreRefundStatus() != null
                        ? refund.getPreRefundStatus()
                        : OrderStatusEnum.PENDING_SHIPMENT.getCode();
                order.setOrderStatus(restoreStatus);
                orderMainMapper.updateById(order);
                log.info("[Refund] Rejected refund, order status reverted: orderId={}, status={}", order.getId(), restoreStatus);
            }
        }

        orderRefundMapper.updateById(refund);
    }

    /**
     * Get refund list for a tenant (merchant).
     */
    public PageResult<OrderRefund> getRefundListByTenant(Long tenantId, int page, int limit) {
        Page<OrderRefund> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(OrderRefund::getTenantId, tenantId);
        }
        wrapper.orderByDesc(OrderRefund::getCreateTime);

        Page<OrderRefund> result = orderRefundMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    /**
     * Get refund list for a member.
     */
    public PageResult<OrderRefund> getRefundListByMember(Long memberId, int page, int limit) {
        Page<OrderRefund> pageParam = new Page<>(page, limit);
        Page<OrderRefund> result = orderRefundMapper.selectPage(pageParam,
                new LambdaQueryWrapper<OrderRefund>()
                        .eq(OrderRefund::getMemberId, memberId)
                        .orderByDesc(OrderRefund::getCreateTime));
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    /**
     * Get refund detail for a member (ensures the refund belongs to the requesting member).
     */
    public OrderRefund getRefundDetailByMember(Long memberId, Long refundId) {
        OrderRefund refund = orderRefundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (!refund.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return refund;
    }
}
