package com.mall.service.payment;

import com.mall.common.enums.PayTypeEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberMapper;
import com.mall.dao.mapper.PaymentRecordMapper;
import com.mall.model.entity.Member;
import com.mall.model.entity.PaymentRecord;
import com.mall.model.entity.PaymentRefund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Balance payment strategy.
 * Deducts member balance directly with database row lock.
 */
@Slf4j
@Component("balancePayStrategy")
@RequiredArgsConstructor
public class BalancePayStrategy implements PayStrategy {

    private final PaymentRecordMapper paymentRecordMapper;
    private final MemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPay(PaymentRecord record) {
        log.info("[Balance Pay] createPay: paymentNo={}, memberId={}, amount={}",
                record.getPaymentNo(), record.getMemberId(), record.getPayAmount());

        // Row-level lock: UPDATE member SET balance = balance - amount WHERE balance >= amount
        int rows = paymentRecordMapper.deductBalance(record.getMemberId(), record.getPayAmount());
        if (rows == 0) {
            throw new BusinessException(ResultCode.PAYMENT_FAIL.getCode(), "余额不足");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("payType", PayTypeEnum.BALANCE.getCode());
        result.put("paymentNo", record.getPaymentNo());
        result.put("success", true);
        return result;
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        // Balance payment is synchronous, no async notify
        return true;
    }

    @Override
    public PayNotifyResult handleNotify(Map<String, String> params) {
        // Balance payment is synchronous, notify is handled directly in createPay
        String paymentNo = params.getOrDefault("paymentNo", "");
        return new PayNotifyResult(true, paymentNo, "", BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResult refund(PaymentRefund record) {
        log.info("[Balance Pay] refund: refundNo={}, memberId={}, amount={}",
                record.getRefundNo(), record.getMemberId(), record.getRefundAmount());

        // Return balance to member
        int rows = paymentRecordMapper.addBalance(record.getMemberId(), record.getRefundAmount());
        if (rows == 0) {
            return new RefundResult(false, record.getRefundNo(), "", "会员不存在");
        }
        return new RefundResult(true, record.getRefundNo(), "balance_refund_" + record.getRefundNo(), "余额退款成功");
    }
}
