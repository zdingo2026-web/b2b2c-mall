package com.mall.api.controller;

import com.mall.service.payment.PaymentService;
import com.mall.service.payment.RefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment notify controller (no login required).
 * Receives payment and refund callbacks from third-party channels.
 */
@Api(tags = "支付回调通知")
@Slf4j
@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class PaymentNotifyController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @ApiOperation("支付回调通知")
    @PostMapping("/pay/{channel}")
    public String payNotify(@PathVariable String channel, HttpServletRequest request) {
        log.info("[Notify] Received pay notify: channel={}", channel);
        try {
            Map<String, String> params = extractParams(request);
            paymentService.handleNotify(channel, params);
            return "success";
        } catch (Exception e) {
            log.error("[Notify] Failed to process pay notify: channel={}", channel, e);
            return "fail";
        }
    }

    @ApiOperation("退款回调通知")
    @PostMapping("/refund/{channel}")
    public String refundNotify(@PathVariable String channel, HttpServletRequest request) {
        log.info("[Notify] Received refund notify: channel={}", channel);
        try {
            Map<String, String> params = extractParams(request);
            refundService.handleRefundNotify(channel, params);
            return "success";
        } catch (Exception e) {
            log.error("[Notify] Failed to process refund notify: channel={}", channel, e);
            return "fail";
        }
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }
}
