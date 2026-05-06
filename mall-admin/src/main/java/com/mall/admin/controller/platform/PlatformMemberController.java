package com.mall.admin.controller.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.dao.mapper.MemberMapper;
import com.mall.model.entity.Member;
import com.mall.model.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Platform member management controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/platform/member")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformMemberController {

    private final MemberMapper memberMapper;

    @GetMapping("/list")
    public R<PageResult<MemberVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Page<Member> pageParam = new Page<>(page, limit);
        Page<Member> result = memberMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Member>().orderByDesc(Member::getCreateTime));

        List<MemberVO> voList = result.getRecords().stream().map(m -> {
            MemberVO vo = new MemberVO();
            vo.setId(m.getId());
            vo.setUsername(m.getUsername());
            vo.setPhone(m.getPhone());
            vo.setNickname(m.getNickname());
            vo.setAvatar(m.getAvatar());
            vo.setGender(m.getGender());
            vo.setEmail(m.getEmail());
            vo.setStatus(m.getStatus());
            vo.setCreateTime(m.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        return R.ok(PageResult.of(voList, result.getTotal(), page, limit));
    }

    @GetMapping("/{id}")
    public R<MemberVO> detail(@PathVariable Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            return R.fail("用户不存在");
        }
        MemberVO vo = new MemberVO();
        vo.setId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setPhone(member.getPhone());
        vo.setNickname(member.getNickname());
        vo.setAvatar(member.getAvatar());
        vo.setGender(member.getGender());
        vo.setEmail(member.getEmail());
        vo.setStatus(member.getStatus());
        vo.setCreateTime(member.getCreateTime());
        return R.ok(vo);
    }
}
