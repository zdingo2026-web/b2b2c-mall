package com.mall.api.controller;

import com.mall.common.exception.BusinessException;
import com.mall.common.response.R;
import com.mall.common.response.ResultCode;
import com.mall.model.dto.RefundApplyDTO;
import com.mall.model.entity.OrderMain;
import com.mall.service.order.OrderService;
import com.mall.service.payment.PaymentService;
import com.mall.service.payment.RefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Member payment controller (C-end, requires login).
 */
@Api(tags = "会员支付")
@RestController
@RequestMapping("/api/v1/member/payment")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberPaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final OrderService orderService;

    @ApiOperation("创建支付单")
    @PostMapping("/create")
    public R<Map<String, Object>> createPayment(
            @RequestParam @NotNull(message = "订单ID不能为空") Long orderId,
            @RequestParam @NotNull(message = "支付方式不能为空") Integer payType) {
        Map<String, Object> result = paymentService.createPayment(orderId, payType);
        return R.ok(result);
    }

    @ApiOperation("发起支付(余额支付直接扣款，第三方返回支付参数)")
    @PostMapping("/pay")
    public R<Map<String, Object>> pay(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String orderNo,
            @RequestParam @NotNull(message = "支付方式不能为空") Integer payType) {
        Long resolvedOrderId = orderId;
        if (resolvedOrderId == null && orderNo != null) {
            OrderMain order = orderService.getByOrderNo(orderNo);
            if (order != null) {
                resolvedOrderId = order.getId();
            }
        }
        if (resolvedOrderId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单ID或订单号不能同时为空");
        }
        Map<String, Object> result = paymentService.createPayment(resolvedOrderId, payType);
        return R.ok(result);
    }

    @ApiOperation("查询支付状态")
    @GetMapping("/{paymentNo}/status")
    public R<Map<String, Object>> queryPaymentStatus(@PathVariable String paymentNo) {
        Map<String, Object> result = paymentService.queryPaymentStatus(paymentNo);
        return R.ok(result);
    }

    @ApiOperation("申请退款")
    @PostMapping("/refund/apply")
    public R<String> applyRefund(@Valid @RequestBody RefundApplyDTO dto) {
        String refundNo = refundService.createRefund(
                dto.getOrderItemId(),
                dto.getRefundAmount() != null ? dto.getRefundAmount() : BigDecimal.ZERO,
                dto.getRefundReason()
        );
        return R.ok(refundNo);
    }
}
