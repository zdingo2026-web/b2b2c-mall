package com.mall.api.controller.member;

import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.vo.MemberAddressVO;
import com.mall.service.user.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Member address controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/address")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberAddressController {

    private final MemberService memberService;

    @GetMapping("/list")
    public R<List<MemberAddressVO>> list() {
        Long memberId = UserContext.getUserId();
        return R.ok(memberService.getAddressList(memberId));
    }

    @GetMapping("/{id}")
    public R<MemberAddressVO> get(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        return R.ok(memberService.getAddress(id, memberId));
    }

    @PostMapping("/add")
    public R<MemberAddressVO> add(@RequestBody MemberAddressVO dto) {
        Long memberId = UserContext.getUserId();
        return R.ok(memberService.addAddress(memberId, dto));
    }

    @PutMapping("/{id}")
    public R<MemberAddressVO> update(@PathVariable Long id, @RequestBody MemberAddressVO dto) {
        Long memberId = UserContext.getUserId();
        return R.ok(memberService.updateAddress(id, memberId, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        memberService.deleteAddress(id, memberId);
        return R.ok();
    }
}
