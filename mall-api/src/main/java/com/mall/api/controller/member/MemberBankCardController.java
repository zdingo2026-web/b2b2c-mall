package com.mall.api.controller.member;

import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.BankCardAddDTO;
import com.mall.model.entity.MemberBankCard;
import com.mall.model.vo.BankCardVO;
import com.mall.service.user.MemberBankCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 会员银行卡管理 (C端)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/bank-card")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberBankCardController {

    private final MemberBankCardService memberBankCardService;

    @GetMapping("/list")
    public R<List<BankCardVO>> list() {
        Long memberId = UserContext.getUserId();
        return R.ok(memberBankCardService.getCardList(memberId));
    }

    @PostMapping("/add")
    public R<BankCardVO> add(@Valid @RequestBody BankCardAddDTO dto) {
        Long memberId = UserContext.getUserId();
        MemberBankCard card = new MemberBankCard();
        card.setBankName(dto.getBankName());
        card.setBankCode(dto.getBankCode());
        card.setCardNo(dto.getCardNo());
        card.setCardType(dto.getCardType());
        card.setCardColor(dto.getCardColor());
        card.setBankLogo(dto.getBankLogo());
        card.setExpiryDate(dto.getExpiryDate());
        return R.ok(memberBankCardService.addCard(memberId, card));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        memberBankCardService.deleteCard(memberId, id);
        return R.ok();
    }

    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        memberBankCardService.setDefault(memberId, id);
        return R.ok();
    }
}
