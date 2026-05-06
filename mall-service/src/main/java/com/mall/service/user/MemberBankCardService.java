package com.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.MemberBankCardMapper;
import com.mall.model.entity.MemberBankCard;
import com.mall.model.vo.BankCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员银行卡服务
 */
@Service
@RequiredArgsConstructor
public class MemberBankCardService {

    private final MemberBankCardMapper memberBankCardMapper;

    public List<BankCardVO> getCardList(Long memberId) {
        LambdaQueryWrapper<MemberBankCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberBankCard::getMemberId, memberId)
                .orderByDesc(MemberBankCard::getIsDefault)
                .orderByDesc(MemberBankCard::getCreateTime);
        List<MemberBankCard> cards = memberBankCardMapper.selectList(wrapper);
        return cards.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public BankCardVO addCard(Long memberId, MemberBankCard card) {
        card.setMemberId(memberId);
        card.setIsDefault(0);
        // Generate masked card number
        String rawNo = card.getCardNo();
        if (rawNo != null && rawNo.length() >= 4) {
            card.setCardNoMask("**** **** **** " + rawNo.substring(rawNo.length() - 4));
        }
        // Set default color if not specified
        if (card.getCardColor() == null || card.getCardColor().isEmpty()) {
            String[] colors = {"#2563EB", "#DC2626", "#059669"};
            long count = memberBankCardMapper.selectCount(
                    new LambdaQueryWrapper<MemberBankCard>().eq(MemberBankCard::getMemberId, memberId));
            card.setCardColor(colors[(int) (count % colors.length)]);
        }
        memberBankCardMapper.insert(card);
        return toVO(card);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCard(Long memberId, Long cardId) {
        MemberBankCard card = memberBankCardMapper.selectById(cardId);
        if (card == null || !card.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        memberBankCardMapper.deleteById(cardId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long memberId, Long cardId) {
        MemberBankCard card = memberBankCardMapper.selectById(cardId);
        if (card == null || !card.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        // Clear all defaults
        LambdaUpdateWrapper<MemberBankCard> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(MemberBankCard::getMemberId, memberId)
                .set(MemberBankCard::getIsDefault, 0);
        memberBankCardMapper.update(null, clearWrapper);
        // Set new default
        card.setIsDefault(1);
        memberBankCardMapper.updateById(card);
    }

    private BankCardVO toVO(MemberBankCard card) {
        BankCardVO vo = new BankCardVO();
        vo.setId(card.getId());
        vo.setBankName(card.getBankName());
        vo.setBankCode(card.getBankCode());
        vo.setCardNoMask(card.getCardNoMask());
        vo.setCardType(card.getCardType());
        vo.setCardColor(card.getCardColor());
        vo.setBankLogo(card.getBankLogo());
        vo.setExpiryDate(card.getExpiryDate());
        vo.setIsDefault(card.getIsDefault());
        return vo;
    }
}
