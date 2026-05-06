package com.mall.service.payment;

import com.mall.common.enums.PayTypeEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberMapper;
import com.mall.dao.mapper.PaymentRecordMapper;
import com.mall.model.entity.PaymentRefund;
import com.mall.model.entity.PaymentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalancePayStrategyTest {

    @Mock
    private PaymentRecordMapper paymentRecordMapper;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private BalancePayStrategy balancePayStrategy;

    private PaymentRecord record;

    @BeforeEach
    void setUp() {
        record = new PaymentRecord();
        record.setPaymentNo("PAY123456");
        record.setMemberId(1L);
        record.setPayAmount(BigDecimal.valueOf(100.00));
    }

    @Nested
    @DisplayName("createPay")
    class CreatePay {

        @Test
        @DisplayName("Deducts balance and returns success when balance is sufficient")
        void createPay_success() {
            when(paymentRecordMapper.deductBalance(1L, BigDecimal.valueOf(100.00))).thenReturn(1);

            Map<String, Object> result = balancePayStrategy.createPay(record);

            assertThat(result).containsEntry("success", true);
            assertThat(result).containsEntry("payType", PayTypeEnum.BALANCE.getCode());
            assertThat(result).containsEntry("paymentNo", "PAY123456");
        }

        @Test
        @DisplayName("Throws PAYMENT_FAIL when balance is insufficient")
        void createPay_insufficientBalance() {
            when(paymentRecordMapper.deductBalance(1L, BigDecimal.valueOf(100.00))).thenReturn(0);

            assertThatThrownBy(() -> balancePayStrategy.createPay(record))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.PAYMENT_FAIL.getCode());
        }
    }

    @Nested
    @DisplayName("verifyNotify")
    class VerifyNotify {

        @Test
        @DisplayName("Always returns true (synchronous payment)")
        void verifyNotify_alwaysTrue() {
            Map<String, String> emptyMap = new HashMap<>();
            assertThat(balancePayStrategy.verifyNotify(emptyMap)).isTrue();
        }
    }

    @Nested
    @DisplayName("handleNotify")
    class HandleNotify {

        @Test
        @DisplayName("Returns success with payment number")
        void handleNotify_returnsSuccess() {
            Map<String, String> notifyParams = new HashMap<>();
            notifyParams.put("paymentNo", "PAY123");
            PayStrategy.PayNotifyResult result = balancePayStrategy.handleNotify(notifyParams);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getPaymentNo()).isEqualTo("PAY123");
        }
    }

    @Nested
    @DisplayName("refund")
    class Refund {

        private PaymentRefund refundRecord;

        @BeforeEach
        void setUp() {
            refundRecord = new PaymentRefund();
            refundRecord.setRefundNo("REF123");
            refundRecord.setMemberId(1L);
            refundRecord.setRefundAmount(BigDecimal.valueOf(50.00));
        }

        @Test
        @DisplayName("Refund succeeds when member exists")
        void refund_success() {
            when(paymentRecordMapper.addBalance(1L, BigDecimal.valueOf(50.00))).thenReturn(1);

            PayStrategy.RefundResult result = balancePayStrategy.refund(refundRecord);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getRefundNo()).isEqualTo("REF123");
        }

        @Test
        @DisplayName("Refund fails when member does not exist")
        void refund_memberNotFound() {
            when(paymentRecordMapper.addBalance(1L, BigDecimal.valueOf(50.00))).thenReturn(0);

            PayStrategy.RefundResult result = balancePayStrategy.refund(refundRecord);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("会员不存在");
        }
    }
}
