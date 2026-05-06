package com.mall.api.controller.member;

import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.vo.MemberAssetVO;
import com.mall.model.vo.MemberVO;
import com.mall.service.user.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Member profile controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/member")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/info")
    public R<MemberVO> getMemberInfo() {
        Long memberId = UserContext.getUserId();
        return R.ok(memberService.getMemberInfo(memberId));
    }

    @PutMapping("/info")
    public R<MemberVO> updateMemberInfo(@RequestBody MemberVO dto) {
        Long memberId = UserContext.getUserId();
        return R.ok(memberService.updateMemberInfo(memberId, dto));
    }

    @GetMapping("/assets")
    public R<MemberAssetVO> getMemberAssets() {
        Long memberId = UserContext.getUserId();
        return R.ok(memberService.getMemberAssets(memberId));
    }
}
