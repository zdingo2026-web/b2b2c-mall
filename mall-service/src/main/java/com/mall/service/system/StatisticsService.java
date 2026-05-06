package com.mall.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.enums.OrderStatusEnum;
import com.mall.dao.mapper.MemberMapper;
import com.mall.dao.mapper.OrderItemMapper;
import com.mall.dao.mapper.OrderMainMapper;
import com.mall.model.entity.Member;
import com.mall.model.entity.OrderItem;
import com.mall.model.entity.OrderMain;
import com.mall.model.vo.MerchantOverviewVO;
import com.mall.model.vo.PlatformOverviewVO;
import com.mall.model.vo.ProductRankVO;
import com.mall.model.vo.TrendDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Statistics service.
 * Provides dashboard overview data for platform and merchant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;

    /**
     * Get platform overview statistics.
     *
     * @return PlatformOverviewVO
     */
    public PlatformOverviewVO getPlatformOverview() {
        PlatformOverviewVO vo = new PlatformOverviewVO();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        // Today's orders
        LambdaQueryWrapper<OrderMain> todayOrderQuery = new LambdaQueryWrapper<>();
        todayOrderQuery.ge(OrderMain::getCreateTime, todayStart)
                .le(OrderMain::getCreateTime, todayEnd)
                .ne(OrderMain::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
        List<OrderMain> todayOrders = orderMainMapper.selectList(todayOrderQuery);
        vo.setTodayOrderCount(todayOrders.size());
        vo.setTodayOrderAmount(todayOrders.stream()
                .map(OrderMain::getPayAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Yesterday's orders
        LambdaQueryWrapper<OrderMain> yesterdayOrderQuery = new LambdaQueryWrapper<>();
        yesterdayOrderQuery.ge(OrderMain::getCreateTime, yesterdayStart)
                .le(OrderMain::getCreateTime, yesterdayEnd)
                .ne(OrderMain::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
        List<OrderMain> yesterdayOrders = orderMainMapper.selectList(yesterdayOrderQuery);
        vo.setYesterdayOrderCount(yesterdayOrders.size());
        vo.setYesterdayOrderAmount(yesterdayOrders.stream()
                .map(OrderMain::getPayAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Today's new members
        LambdaQueryWrapper<Member> todayMemberQuery = new LambdaQueryWrapper<>();
        todayMemberQuery.ge(Member::getCreateTime, todayStart)
                .le(Member::getCreateTime, todayEnd);
        vo.setTodayNewMember(Math.toIntExact(memberMapper.selectCount(todayMemberQuery)));

        // Today's visit count (MVP: mock)
        vo.setTodayVisitCount(0);

        return vo;
    }

    /**
     * Get merchant overview statistics.
     *
     * @param tenantId merchant tenant ID
     * @return MerchantOverviewVO
     */
    public MerchantOverviewVO getMerchantOverview(Long tenantId) {
        MerchantOverviewVO vo = new MerchantOverviewVO();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        // Today's orders for this tenant
        LambdaQueryWrapper<OrderMain> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(OrderMain::getTenantId, tenantId)
                .ge(OrderMain::getCreateTime, todayStart)
                .le(OrderMain::getCreateTime, todayEnd)
                .ne(OrderMain::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
        List<OrderMain> todayOrders = orderMainMapper.selectList(todayQuery);
        BigDecimal todayAmount = todayOrders.stream()
                .map(OrderMain::getPayAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodayOrderCount(todayOrders.size());
        vo.setTodayOrderAmount(todayAmount);
        vo.setTodayAvgOrderPrice(todayOrders.isEmpty() ? BigDecimal.ZERO :
                todayAmount.divide(BigDecimal.valueOf(todayOrders.size()), 2, RoundingMode.HALF_UP));

        // Yesterday's orders for this tenant
        LambdaQueryWrapper<OrderMain> yesterdayQuery = new LambdaQueryWrapper<>();
        yesterdayQuery.eq(OrderMain::getTenantId, tenantId)
                .ge(OrderMain::getCreateTime, yesterdayStart)
                .le(OrderMain::getCreateTime, yesterdayEnd)
                .ne(OrderMain::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
        List<OrderMain> yesterdayOrders = orderMainMapper.selectList(yesterdayQuery);
        BigDecimal yesterdayAmount = yesterdayOrders.stream()
                .map(OrderMain::getPayAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setYesterdayOrderCount(yesterdayOrders.size());
        vo.setYesterdayOrderAmount(yesterdayAmount);
        vo.setYesterdayAvgOrderPrice(yesterdayOrders.isEmpty() ? BigDecimal.ZERO :
                yesterdayAmount.divide(BigDecimal.valueOf(yesterdayOrders.size()), 2, RoundingMode.HALF_UP));

        // Total order amount for this tenant
        LambdaQueryWrapper<OrderMain> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.eq(OrderMain::getTenantId, tenantId)
                .ne(OrderMain::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
        List<OrderMain> allOrders = orderMainMapper.selectList(totalQuery);
        vo.setTotalOrderAmount(allOrders.stream()
                .map(OrderMain::getPayAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Shop conversion rate (MVP: mock)
        vo.setShopConversionRate(BigDecimal.ZERO);

        return vo;
    }

    /**
     * Get merchant trend data for the last N days.
     *
     * @param tenantId merchant tenant ID
     * @param days     number of days
     * @return list of TrendDataVO
     */
    public List<TrendDataVO> getMerchantTrend(Long tenantId, int days) {
        List<TrendDataVO> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<OrderMain> query = new LambdaQueryWrapper<>();
            query.eq(OrderMain::getTenantId, tenantId)
                    .ge(OrderMain::getCreateTime, dayStart)
                    .le(OrderMain::getCreateTime, dayEnd)
                    .ne(OrderMain::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
            List<OrderMain> orders = orderMainMapper.selectList(query);

            TrendDataVO vo = new TrendDataVO();
            vo.setDate(date.format(fmt));
            vo.setOrderCount(orders.size());
            vo.setOrderAmount(orders.stream()
                    .map(OrderMain::getPayAmount)
                    .filter(a -> a != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(vo);
        }

        return result;
    }

    /**
     * Get merchant product ranking by sales (top 10).
     *
     * @param tenantId merchant tenant ID
     * @return list of ProductRankVO
     */
    public List<ProductRankVO> getMerchantProductRanking(Long tenantId) {
        LambdaQueryWrapper<OrderItem> query = new LambdaQueryWrapper<>();
        query.eq(OrderItem::getTenantId, tenantId);
        List<OrderItem> items = orderItemMapper.selectList(query);

        // Group by spuId, sum quantity and payAmount
        Map<Long, ProductRankVO> rankMap = new LinkedHashMap<>();
        for (OrderItem item : items) {
            ProductRankVO vo = rankMap.computeIfAbsent(item.getSpuId(), k -> {
                ProductRankVO v = new ProductRankVO();
                v.setSpuId(k);
                v.setProductName(item.getProductName());
                v.setMainImage(item.getProductImage());
                v.setTotalSales(0);
                v.setTotalAmount(BigDecimal.ZERO);
                return v;
            });
            vo.setTotalSales(vo.getTotalSales() + item.getQuantity());
            if (item.getPayAmount() != null) {
                vo.setTotalAmount(vo.getTotalAmount().add(item.getPayAmount()));
            }
        }

        return rankMap.values().stream()
                .sorted(Comparator.comparing(ProductRankVO::getTotalSales).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
}
