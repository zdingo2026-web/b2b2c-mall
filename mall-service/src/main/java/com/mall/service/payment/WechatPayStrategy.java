package com.mall.service.payment;

import com.mall.model.entity.PaymentRecord;
import com.mall.model.entity.PaymentRefund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * WeChat Pay strategy implementation.
 * MVP: Mock implementation. Real implementation requires merchant credentials.
 */
@Slf4j
@Component("wechatPayStrategy")
public class WechatPayStrategy implements PayStrategy {

    @Override
    public Map<String, Object> createPay(PaymentRecord record) {
        log.info("[WeChat Pay] Mock createPay: paymentNo={}, amount={}", record.getPaymentNo(), record.getPayAmount());

        Map<String, Object> result = new HashMap<>();
        result.put("appId", "wx_mock_app_id");
        result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        result.put("nonceStr", "mock_nonce_" + record.getPaymentNo());
        result.put("package", "prepay_id=wx_mock_prepay_" + record.getPaymentNo());
        result.put("signType", "RSA");
        result.put("paySign", "mock_sign_value");
        return result;
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        log.info("[WeChat Pay] Mock verifyNotify: params={}", params);
        return true;
    }

    @Override
    public PayNotifyResult handleNotify(Map<String, String> params) {
        log.info("[WeChat Pay] Mock handleNotify: params={}", params);
        String paymentNo = params.getOrDefault("out_trade_no", "");
        String tradeNo = params.getOrDefault("transaction_id", "wx_mock_trade_" + System.currentTimeMillis());
        BigDecimal payAmount = new BigDecimal(params.getOrDefault("total_fee", "0")).divide(new BigDecimal("100"));
        return new PayNotifyResult(true, paymentNo, tradeNo, payAmount);
    }

    @Override
    public RefundResult refund(PaymentRefund record) {
        log.info("[WeChat Pay] Mock refund: refundNo={}, amount={}", record.getRefundNo(), record.getRefundAmount());
        return new RefundResult(true, record.getRefundNo(), "wx_mock_refund_" + System.currentTimeMillis(), "Mock refund success");
    }
}
