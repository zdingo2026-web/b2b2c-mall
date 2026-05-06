package com.mall.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.enums.OrderStatusEnum;
import com.mall.common.enums.ProductStatusEnum;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.*;
import com.mall.model.dto.OrderCreateDTO;
import com.mall.model.entity.*;
import com.mall.service.order.OrderStateMachine.OrderEvent;
import com.mall.service.product.StockService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock private OrderMainMapper orderMainMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private OrderAddressMapper orderAddressMapper;
    @Mock private OrderLogMapper orderLogMapper;
    @Mock private CartService cartService;
    @Mock private StockService stockService;
    @Mock private OrderStateMachine orderStateMachine;
    @Mock private MemberAddressMapper memberAddressMapper;
    @Mock private ProductSkuMapper productSkuMapper;
    @Mock private ProductSpuMapper productSpuMapper;

    @InjectMocks
    private OrderService orderService;

    private static final Long MEMBER_ID = 1L;
    private static final Long ADDRESS_ID = 10L;
    private static final Long SKU_ID = 100L;
    private static final Long SPU_ID = 50L;
    private static final Long TENANT_ID = 5L;

    private MemberAddress address;
    private ProductSku sku;
    private ProductSpu spu;
    private OrderMain order;

    @BeforeEach
    void setUp() {
        address = new MemberAddress();
        address.setId(ADDRESS_ID);
        address.setReceiverName("张三");
        address.setReceiverPhone("13800138000");
        address.setProvinceName("广东省");
        address.setCityName("深圳市");
        address.setDistrictName("南山区");
        address.setDetailAddress("科技园路1号");

        sku = new ProductSku();
        sku.setId(SKU_ID);
        sku.setSpuId(SPU_ID);
        sku.setPrice(BigDecimal.valueOf(99.90));
        sku.setSpecValues("颜色:红色");
        sku.setImage("sku_img.jpg");

        spu = new ProductSpu();
        spu.setId(SPU_ID);
        spu.setTenantId(TENANT_ID);
        spu.setProductName("测试商品");
        spu.setMainImage("main_img.jpg");
        spu.setStatus(ProductStatusEnum.ON_SHELF.getCode());

        order = new OrderMain();
        order.setId(1L);
        order.setOrderNo("ORD123");
        order.setMemberId(MEMBER_ID);
        order.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setPayAmount(BigDecimal.valueOf(199.80));
        order.setTotalAmount(BigDecimal.valueOf(199.80));
        order.setTenantId(TENANT_ID);
        order.setCreateTime(java.time.LocalDateTime.now());

        OrderLog logEntry = new OrderLog();
        doReturn(logEntry).when(orderStateMachine).buildOrderLog(any(), any(), anyInt(), anyString(),
                any(OrderEvent.class), anyInt());
    }

    @Nested
    @DisplayName("createOrder - validation")
    class CreateOrder {

        @Test
        @DisplayName("Throws SKU_NOT_FOUND when SKU does not exist")
        void createOrder_skuNotFound() {
            OrderCreateDTO.OrderItemDTO itemDTO = new OrderCreateDTO.OrderItemDTO();
            itemDTO.setSkuId(999L);
            itemDTO.setQuantity(1);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setAddressId(ADDRESS_ID);
            dto.setItems(Collections.singletonList(itemDTO));

            when(productSkuMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> orderService.createOrder(MEMBER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.SKU_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Throws error when address does not exist")
        void createOrder_addressNotFound() {
            OrderCreateDTO.OrderItemDTO itemDTO = new OrderCreateDTO.OrderItemDTO();
            itemDTO.setSkuId(SKU_ID);
            itemDTO.setQuantity(1);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setAddressId(999L);
            dto.setItems(Collections.singletonList(itemDTO));

            when(productSkuMapper.selectById(SKU_ID)).thenReturn(sku);
            when(productSpuMapper.selectById(SPU_ID)).thenReturn(spu);
            when(memberAddressMapper.selectById(999L)).thenReturn(null);
            when(productSpuMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(spu);

            assertThatThrownBy(() -> orderService.createOrder(MEMBER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("收货地址");
        }

        @Test
        @DisplayName("Throws error when cart is empty and no direct items")
        void createOrder_emptyCart() {
            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setAddressId(ADDRESS_ID);
            dto.setItems(null);

            when(cartService.getCheckedItems(MEMBER_ID)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> orderService.createOrder(MEMBER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("请选择");
        }

        @Test
        @DisplayName("Deducts stock for each item in direct purchase")
        void createOrder_deductsStock() {
            OrderCreateDTO.OrderItemDTO itemDTO = new OrderCreateDTO.OrderItemDTO();
            itemDTO.setSkuId(SKU_ID);
            itemDTO.setQuantity(2);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setAddressId(ADDRESS_ID);
            dto.setItems(Collections.singletonList(itemDTO));

            when(productSkuMapper.selectById(SKU_ID)).thenReturn(sku);
            when(productSpuMapper.selectById(SPU_ID)).thenReturn(spu);
            when(memberAddressMapper.selectById(ADDRESS_ID)).thenReturn(address);
            when(orderMainMapper.insert(any(OrderMain.class))).thenReturn(1);
            when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
            when(orderAddressMapper.insert(any(OrderAddress.class))).thenReturn(1);
            when(orderLogMapper.insert(any(OrderLog.class))).thenReturn(1);
            when(orderMainMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
            when(orderAddressMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(productSpuMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(spu);

            orderService.createOrder(MEMBER_ID, dto);

            verify(stockService).deductStock(SKU_ID, 2);
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("Cancels order and restores stock")
        void cancelOrder_success() {
            order.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());

            OrderItem item = new OrderItem();
            item.setSkuId(SKU_ID);
            item.setQuantity(2);

            when(orderMainMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
            when(orderStateMachine.fireEvent(OrderStatusEnum.PENDING_PAYMENT.getCode(), OrderEvent.CANCEL))
                    .thenReturn(OrderStatusEnum.CANCELLED.getCode());
            when(orderMainMapper.updateById(any(OrderMain.class))).thenReturn(1);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(item));
            when(orderLogMapper.insert(any(OrderLog.class))).thenReturn(1);

            orderService.cancelOrder(MEMBER_ID, "ORD123");

            verify(stockService).addStock(SKU_ID, 2);
            verify(orderMainMapper).updateById(any(OrderMain.class));
        }

        @Test
        @DisplayName("Throws ORDER_NOT_FOUND when order does not exist")
        void cancelOrder_orderNotFound() {
            when(orderMainMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> orderService.cancelOrder(MEMBER_ID, "INVALID"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Throws FORBIDDEN when order belongs to another member")
        void cancelOrder_wrongMember() {
            order.setMemberId(999L);

            when(orderMainMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

            assertThatThrownBy(() -> orderService.cancelOrder(MEMBER_ID, "ORD123"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.FORBIDDEN.getCode());
        }
    }

    @Nested
    @DisplayName("confirmReceive")
    class ConfirmReceive {

        @Test
        @DisplayName("Confirms receipt and updates order status")
        void confirmReceive_success() {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(1L);
            orderItem.setOrderId(1L);

            order.setOrderStatus(OrderStatusEnum.PENDING_RECEIPT.getCode());

            when(orderItemMapper.selectById(1L)).thenReturn(orderItem);
            when(orderMainMapper.selectById(1L)).thenReturn(order);
            when(orderStateMachine.fireEvent(OrderStatusEnum.PENDING_RECEIPT.getCode(), OrderEvent.CONFIRM))
                    .thenReturn(OrderStatusEnum.COMPLETED.getCode());
            when(orderMainMapper.updateById(any(OrderMain.class))).thenReturn(1);
            when(orderLogMapper.insert(any(OrderLog.class))).thenReturn(1);

            orderService.confirmReceive(MEMBER_ID, 1L);

            verify(orderMainMapper).updateById(any(OrderMain.class));
        }

        @Test
        @DisplayName("Throws DATA_NOT_FOUND when order item does not exist")
        void confirmReceive_itemNotFound() {
            when(orderItemMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> orderService.confirmReceive(MEMBER_ID, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.DATA_NOT_FOUND.getCode());
        }
    }

    @Nested
    @DisplayName("shipOrder")
    class ShipOrder {

        @Test
        @DisplayName("Ships order with logistics info")
        void shipOrder_success() {
            order.setOrderStatus(OrderStatusEnum.PENDING_SHIPMENT.getCode());

            OrderItem orderItem = new OrderItem();
            orderItem.setId(1L);
            orderItem.setOrderId(1L);

            when(orderItemMapper.selectById(1L)).thenReturn(orderItem);
            when(orderMainMapper.selectById(1L)).thenReturn(order);
            when(orderStateMachine.fireEvent(OrderStatusEnum.PENDING_SHIPMENT.getCode(), OrderEvent.SHIP))
                    .thenReturn(OrderStatusEnum.PENDING_RECEIPT.getCode());
            when(orderMainMapper.updateById(any(OrderMain.class))).thenReturn(1);
            when(orderLogMapper.insert(any(OrderLog.class))).thenReturn(1);

            orderService.shipOrder(1L, "顺丰速运", "SF123456");

            verify(orderMainMapper).updateById(any(OrderMain.class));
            assertThat(order.getLogisticsCompany()).isEqualTo("顺丰速运");
            assertThat(order.getLogisticsNo()).isEqualTo("SF123456");
        }

        @Test
        @DisplayName("Throws ORDER_NOT_FOUND when order item does not exist")
        void shipOrder_orderNotFound() {
            when(orderItemMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> orderService.shipOrder(999L, "顺丰速运", "SF123"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ResultCode.ORDER_NOT_FOUND.getCode());
        }
    }
}
