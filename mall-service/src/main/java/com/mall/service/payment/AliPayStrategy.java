package com.mall.service.payment;

import com.mall.model.entity.PaymentRecord;
import com.mall.model.entity.PaymentRefund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * AliPay strategy implementation.
 * MVP: Mock implementation. Real implementation requires merchant credentials.
 */
@Slf4j
@Component("aliPayStrategy")
public class AliPayStrategy implements PayStrategy {

    @Override
    public Map<String, Object> createPay(PaymentRecord record) {
        log.info("[AliPay] Mock createPay: paymentNo={}, amount={}", record.getPaymentNo(), record.getPayAmount());

        Map<String, Object> result = new HashMap<>();
        result.put("payUrl", "https://openapi.alipay.com/gateway.do?mock_pay_url_" + record.getPaymentNo());
        result.put("outTradeNo", record.getPaymentNo());
        return result;
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        log.info("[AliPay] Mock verifyNotify: params={}", params);
        return true;
    }

    @Override
    public PayNotifyResult handleNotify(Map<String, String> params) {
        log.info("[AliPay] Mock handleNotify: params={}", params);
        String paymentNo = params.getOrDefault("out_trade_no", "");
        String tradeNo = params.getOrDefault("trade_no", "alipay_mock_trade_" + System.currentTimeMillis());
        BigDecimal payAmount = new BigDecimal(params.getOrDefault("total_amount", "0"));
        return new PayNotifyResult(true, paymentNo, tradeNo, payAmount);
    }

    @Override
    public RefundResult refund(PaymentRefund record) {
        log.info("[AliPay] Mock refund: refundNo={}, amount={}", record.getRefundNo(), record.getRefundAmount());
        return new RefundResult(true, record.getRefundNo(), "alipay_mock_refund_" + System.currentTimeMillis(), "Mock refund success");
    }
}
