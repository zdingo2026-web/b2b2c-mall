package com.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.common.util.SnowflakeIdUtil;
import com.mall.dao.mapper.MemberAddressMapper;
import com.mall.dao.mapper.points.PointsAccountMapper;
import com.mall.dao.mapper.points.PointsOrderMapper;
import com.mall.dao.mapper.points.PointsProductMapper;
import com.mall.model.dto.points.PointsOrderCreateDTO;
import com.mall.model.entity.MemberAddress;
import com.mall.model.entity.points.PointsAccount;
import com.mall.model.entity.points.PointsOrder;
import com.mall.model.entity.points.PointsProduct;
import com.mall.model.vo.points.PointsOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsOrderService {

    private final PointsOrderMapper pointsOrderMapper;
    private final PointsAccountMapper pointsAccountMapper;
    private final PointsProductMapper pointsProductMapper;
    private final MemberAddressMapper memberAddressMapper;

    private static final int ORDER_STATUS_PENDING = 1;
    private static final int ORDER_STATUS_SHIPPED = 2;
    private static final int ORDER_STATUS_RECEIVED = 3;
    private static final int ORDER_STATUS_CANCELLED = 4;

    @Transactional(rollbackFor = Exception.class)
    public PointsOrderVO createOrder(Long memberId, PointsOrderCreateDTO dto) {
        PointsProduct product = pointsProductMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品已下架");
        }
        if (product.getStock() < dto.getQuantity()) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        int pointsNeeded = product.getPointsPrice() * dto.getQuantity();
        PointsAccount account = pointsAccountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getMemberId, memberId));
        if (account == null || account.getBalance() < pointsNeeded) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "积分不足");
        }

        MemberAddress address = memberAddressMapper.selectById(dto.getAddressId());
        if (address == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "收货地址不存在");
        }

        account.setBalance(account.getBalance() - pointsNeeded);
        account.setTotalSpent(account.getTotalSpent() + pointsNeeded);
        pointsAccountMapper.updateById(account);

        product.setStock(product.getStock() - dto.getQuantity());
        product.setSales(product.getSales() + dto.getQuantity());
        pointsProductMapper.updateById(product);

        PointsOrder order = new PointsOrder();
        order.setOrderNo(String.valueOf(SnowflakeIdUtil.getInstance().nextId()));
        order.setMemberId(memberId);
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        order.setProductImage(product.getProductImage());
        order.setExchangeType(dto.getExchangeType());
        order.setPointsAmount(pointsNeeded);
        order.setCashAmount(product.getCashPrice());
        order.setConsigneeName(address.getReceiverName());
        order.setConsigneePhone(address.getReceiverPhone());
        order.setConsigneeAddress(address.getProvinceName() + address.getCityName()
                + address.getDistrictName() + address.getDetailAddress());
        order.setStatus(ORDER_STATUS_PENDING);
        order.setTenantId(product.getTenantId());
        pointsOrderMapper.insert(order);

        return toVO(order);
    }

    public PointsOrderVO orderDetail(Long id, Long memberId) {
        PointsOrder order = pointsOrderMapper.selectById(id);
        if (order == null || !order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return toVO(order);
    }

    public PageResult<PointsOrderVO> orderList(Long memberId, Integer status, int page, int limit) {
        Page<PointsOrder> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<PointsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsOrder::getMemberId, memberId);
        if (status != null) {
            wrapper.eq(PointsOrder::getStatus, status);
        }
        wrapper.orderByDesc(PointsOrder::getCreateTime);
        Page<PointsOrder> result = pointsOrderMapper.selectPage(pageParam, wrapper);
        List<PointsOrderVO> voList = result.getRecords().stream()
                .map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id, Long memberId) {
        PointsOrder order = pointsOrderMapper.selectById(id);
        if (order == null || !order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (order.getStatus() != ORDER_STATUS_PENDING) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        PointsAccount account = pointsAccountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getMemberId, memberId));
        if (account != null) {
            account.setBalance(account.getBalance() + order.getPointsAmount());
            account.setTotalSpent(account.getTotalSpent() - order.getPointsAmount());
            pointsAccountMapper.updateById(account);
        }

        PointsProduct product = pointsProductMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStock(product.getStock() + 1);
            product.setSales(product.getSales() - 1);
            pointsProductMapper.updateById(product);
        }

        order.setStatus(ORDER_STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        pointsOrderMapper.updateById(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long id, String trackingNo) {
        PointsOrder order = pointsOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (order.getStatus() != ORDER_STATUS_PENDING) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(ORDER_STATUS_SHIPPED);
        order.setShipTime(LocalDateTime.now());
        pointsOrderMapper.updateById(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void receiveOrder(Long id, Long memberId) {
        PointsOrder order = pointsOrderMapper.selectById(id);
        if (order == null || !order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (order.getStatus() != ORDER_STATUS_SHIPPED) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(ORDER_STATUS_RECEIVED);
        order.setReceiveTime(LocalDateTime.now());
        pointsOrderMapper.updateById(order);
    }

    private PointsOrderVO toVO(PointsOrder order) {
        PointsOrderVO vo = new PointsOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setProductId(order.getProductId());
        vo.setProductName(order.getProductName());
        vo.setProductImage(order.getProductImage());
        vo.setExchangeType(order.getExchangeType());
        vo.setPointsAmount(order.getPointsAmount());
        vo.setCashAmount(order.getCashAmount());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }
}
