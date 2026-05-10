package com.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.member.MemberCheckinRecordMapper;
import com.mall.dao.mapper.points.PointsAccountMapper;
import com.mall.dao.mapper.points.PointsDetailMapper;
import com.mall.dao.mapper.points.PointsRuleMapper;
import com.mall.model.entity.member.MemberCheckinRecord;
import com.mall.model.entity.points.PointsAccount;
import com.mall.model.entity.points.PointsDetail;
import com.mall.model.entity.points.PointsRule;
import com.mall.model.vo.points.CheckinResultVO;
import com.mall.model.vo.points.CheckinStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsCheckinService {

    private final PointsAccountMapper pointsAccountMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final MemberCheckinRecordMapper memberCheckinRecordMapper;
    private final PointsRuleMapper pointsRuleMapper;

    private static final int CHECKIN_RULE_TYPE = 1;
    private static final int BONUS_DAYS = 7;

    @Transactional(rollbackFor = Exception.class)
    public CheckinResultVO checkin(Long memberId) {
        LocalDate today = LocalDate.now();
        MemberCheckinRecord todayRecord = memberCheckinRecordMapper.selectOne(
                new LambdaQueryWrapper<MemberCheckinRecord>()
                        .eq(MemberCheckinRecord::getMemberId, memberId)
                        .eq(MemberCheckinRecord::getCheckinDate, today));
        if (todayRecord != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "今日已签到");
        }

        PointsRule checkinRule = pointsRuleMapper.selectOne(
                new LambdaQueryWrapper<PointsRule>()
                        .eq(PointsRule::getRuleType, CHECKIN_RULE_TYPE)
                        .eq(PointsRule::getEnabled, 1)
                        .last("LIMIT 1"));
        int basePoints = (checkinRule != null && checkinRule.getPointsValue() != null)
                ? checkinRule.getPointsValue() : 10;

        MemberCheckinRecord yesterdayRecord = memberCheckinRecordMapper.selectOne(
                new LambdaQueryWrapper<MemberCheckinRecord>()
                        .eq(MemberCheckinRecord::getMemberId, memberId)
                        .eq(MemberCheckinRecord::getCheckinDate, today.minusDays(1)));
        int continuousDays = (yesterdayRecord != null) ? yesterdayRecord.getContinuousDays() + 1 : 1;

        int bonusPoints = 0;
        boolean sevenDayBonus = false;
        if (continuousDays > 0 && continuousDays % BONUS_DAYS == 0) {
            bonusPoints = basePoints * 2;
            sevenDayBonus = true;
        }

        int totalPoints = basePoints + bonusPoints;

        MemberCheckinRecord record = new MemberCheckinRecord();
        record.setMemberId(memberId);
        record.setCheckinDate(today);
        record.setContinuousDays(continuousDays);
        record.setPointsEarned(basePoints);
        record.setBonusEarned(bonusPoints);
        memberCheckinRecordMapper.insert(record);

        PointsAccount account = pointsAccountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getMemberId, memberId));
        if (account == null) {
            account = new PointsAccount();
            account.setMemberId(memberId);
            account.setBalance(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
            pointsAccountMapper.insert(account);
        }
        account.setBalance(account.getBalance() + totalPoints);
        account.setTotalEarned(account.getTotalEarned() + totalPoints);
        pointsAccountMapper.updateById(account);

        PointsDetail detail = new PointsDetail();
        detail.setMemberId(memberId);
        detail.setBizType(1);
        detail.setBizId(String.valueOf(record.getId()));
        detail.setChangeAmount(totalPoints);
        detail.setChangeType(1);
        detail.setBalanceAfter(account.getBalance());
        detail.setRemark("签到获得积分" + (sevenDayBonus ? "(含连续签到奖励)" : ""));
        detail.setExpired(0);
        pointsDetailMapper.insert(detail);

        CheckinResultVO vo = new CheckinResultVO();
        vo.setPointsEarned(basePoints);
        vo.setBonusEarned(bonusPoints);
        vo.setContinuousDays(continuousDays);
        vo.setSevenDayBonus(sevenDayBonus);
        return vo;
    }

    public CheckinStatusVO checkinStatus(Long memberId) {
        LocalDate today = LocalDate.now();
        MemberCheckinRecord todayRecord = memberCheckinRecordMapper.selectOne(
                new LambdaQueryWrapper<MemberCheckinRecord>()
                        .eq(MemberCheckinRecord::getMemberId, memberId)
                        .eq(MemberCheckinRecord::getCheckinDate, today));

        int continuousDays = 0;
        if (todayRecord != null) {
            continuousDays = todayRecord.getContinuousDays();
        } else {
            MemberCheckinRecord yesterdayRecord = memberCheckinRecordMapper.selectOne(
                    new LambdaQueryWrapper<MemberCheckinRecord>()
                            .eq(MemberCheckinRecord::getMemberId, memberId)
                            .eq(MemberCheckinRecord::getCheckinDate, today.minusDays(1)));
            if (yesterdayRecord != null) {
                continuousDays = yesterdayRecord.getContinuousDays();
            }
        }

        int nextBonusDays = BONUS_DAYS - (continuousDays % BONUS_DAYS);
        if (nextBonusDays == BONUS_DAYS) {
            nextBonusDays = BONUS_DAYS;
        }

        CheckinStatusVO vo = new CheckinStatusVO();
        vo.setTodayChecked(todayRecord != null);
        vo.setContinuousDays(continuousDays);
        vo.setNextBonusDays(nextBonusDays);
        return vo;
    }
}
