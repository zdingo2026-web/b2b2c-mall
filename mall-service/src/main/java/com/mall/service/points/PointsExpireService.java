package com.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.dao.mapper.points.PointsAccountMapper;
import com.mall.dao.mapper.points.PointsDetailMapper;
import com.mall.model.entity.points.PointsAccount;
import com.mall.model.entity.points.PointsDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsExpireService {

    private final PointsAccountMapper pointsAccountMapper;
    private final PointsDetailMapper pointsDetailMapper;

    @Transactional(rollbackFor = Exception.class)
    public void expirePoints() {
        LocalDateTime now = LocalDateTime.now();
        List<PointsDetail> expiredDetails = pointsDetailMapper.selectList(
                new LambdaQueryWrapper<PointsDetail>()
                        .eq(PointsDetail::getExpired, 0)
                        .eq(PointsDetail::getChangeType, 1)
                        .gt(PointsDetail::getChangeAmount, 0)
                        .le(PointsDetail::getExpireTime, now)
                        .isNotNull(PointsDetail::getExpireTime));

        if (expiredDetails.isEmpty()) {
            return;
        }

        for (PointsDetail detail : expiredDetails) {
            detail.setExpired(1);
            pointsDetailMapper.updateById(detail);

            PointsAccount account = pointsAccountMapper.selectOne(
                    new LambdaQueryWrapper<PointsAccount>()
                            .eq(PointsAccount::getMemberId, detail.getMemberId()));
            if (account != null && account.getBalance() >= detail.getChangeAmount()) {
                account.setBalance(account.getBalance() - detail.getChangeAmount());
                pointsAccountMapper.updateById(account);

                PointsDetail expireDetail = new PointsDetail();
                expireDetail.setMemberId(detail.getMemberId());
                expireDetail.setBizType(99);
                expireDetail.setBizId(String.valueOf(detail.getId()));
                expireDetail.setChangeAmount(-detail.getChangeAmount());
                expireDetail.setChangeType(3);
                expireDetail.setBalanceAfter(account.getBalance());
                expireDetail.setRemark("积分过期");
                expireDetail.setExpired(0);
                pointsDetailMapper.insert(expireDetail);
            }
        }
        log.info("积分过期处理完成，处理记录数: {}", expiredDetails.size());
    }
}
