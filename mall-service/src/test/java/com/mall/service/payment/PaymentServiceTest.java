package com.mall.service.payment;

import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.enums.PayTypeEnum;
import com.mall.common.enums.PaymentStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.dao.mapper.PaymentRecordMapper;
import com.mall.model.entity.OrderMain;
import com.mall.model.entity.PaymentRecord;
import com.mall.service.mq.producer.PaymentMessageProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock
    private PaymentRecordMapper paymentRecordMapper;

    @Mock
    private OrderMainMapper orderMainMapper;

    @Mock
    private PaymentFactory paymentFactory;

    @Mock
    private PaymentMessageProducer paymentMessageProducer;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PaymentService paymentService;

    private static final Long ORDER_ID = 1L;
    private static final Long MEMBER_ID = 100L;
    private static final Long TENANT_ID = 5L;

    private OrderMain order;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(MEMBER_ID);
        UserContext.setUserType(3);
        UserContext.setTenantId(null);

        // Mock Redis lock for payment (B-07: distributed lock)
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);

        order = new OrderMain();
        order.setId(ORDER_ID);
        order.setOrderNo("ORD123456");
        order.setMemberId(MEMBER_ID);
        order.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setPayAmount(BigDecimal.valueOf(199.90));
        order.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Nested
    @DisplayName("createPayment - validation")
    class CreatePayment {

        @Test
        @DisplayName("Throws UNAUTHORIZED when user context is missing")
        void createPayment_noUser() {
            UserContext.clear();

            assertThatThrownBy(() -> paymentService.createPayment(ORDER_ID, PayTypeEnum.BALANCE.getCode()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("Throws ORDER_NOT_FOUND when order does not exist")
        void createPayment_orderNotFound() {
            when(orderMainMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> paymentService.createPayment(999L, PayTypeEnum.BALANCE.getCode()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Throws FORBIDDEN when order belongs to another member")
        void createPayment_wrongMember() {
            order.setMemberId(999L);
            when(orderMainMapper.selectById(ORDER_ID)).thenReturn(order);

            assertThatThrownBy(() -> paymentService.createPayment(ORDER_ID, PayTypeEnum.BALANCE.getCode()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("Throws ORDER_CANNOT_PAY when order is not in PENDING_PAYMENT status")
        void createPayment_wrongStatus() {
            order.setOrderStatus(OrderStatusEnum.PENDING_SHIPMENT.getCode());
            when(orderMainMapper.selectById(ORDER_ID)).thenReturn(order);

            assertThatThrownBy(() -> paymentService.createPayment(ORDER_ID, PayTypeEnum.BALANCE.getCode()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.ORDER_CANNOT_PAY.getCode());
        }
    }

    @Nested
    @DisplayName("resolvePayType")
    class ResolvePayType {

        @Test
        @DisplayName("Throws error for unsupported payment channel")
        void resolvePayType_unsupported() {
            assertThatThrownBy(() -> paymentService.handleNotify("unknown", new HashMap<>()))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
