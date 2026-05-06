package com.mall.service.payment;

import com.mall.model.entity.PaymentRecord;
import com.mall.model.entity.PaymentRefund;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Payment strategy interface.
 * Each payment channel (WeChat, AliPay, Balance) implements this interface.
 */
public interface PayStrategy {

    /**
     * Create a payment order with the third-party channel.
     *
     * @param record payment record
     * @return payment parameters for the client (e.g., prepay_id, pay_url)
     */
    Map<String, Object> createPay(PaymentRecord record);

    /**
     * Verify the notify request signature from the payment channel.
     *
     * @param params request parameters from the channel callback
     * @return true if the signature is valid
     */
    boolean verifyNotify(Map<String, String> params);

    /**
     * Parse the notify request and return the payment result.
     *
     * @param params request parameters from the channel callback
     * @return notify result containing paymentNo, tradeNo, and success flag
     */
    PayNotifyResult handleNotify(Map<String, String> params);

    /**
     * Refund via the payment channel.
     *
     * @param record refund record
     * @return refund result
     */
    RefundResult refund(PaymentRefund record);

    /**
     * Payment notify result.
     */
    class PayNotifyResult {
        private final boolean success;
        private final String paymentNo;
        private final String tradeNo;
        private final BigDecimal payAmount;

        public PayNotifyResult(boolean success, String paymentNo, String tradeNo, BigDecimal payAmount) {
            this.success = success;
            this.paymentNo = paymentNo;
            this.tradeNo = tradeNo;
            this.payAmount = payAmount;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getPaymentNo() {
            return paymentNo;
        }

        public String getTradeNo() {
            return tradeNo;
        }

        public BigDecimal getPayAmount() {
            return payAmount;
        }
    }

    /**
     * Refund result.
     */
    class RefundResult {
        private final boolean success;
        private final String refundNo;
        private final String tradeNo;
        private final String message;

        public RefundResult(boolean success, String refundNo, String tradeNo, String message) {
            this.success = success;
            this.refundNo = refundNo;
            this.tradeNo = tradeNo;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getRefundNo() {
            return refundNo;
        }

        public String getTradeNo() {
            return tradeNo;
        }

        public String getMessage() {
            return message;
        }
    }
}
