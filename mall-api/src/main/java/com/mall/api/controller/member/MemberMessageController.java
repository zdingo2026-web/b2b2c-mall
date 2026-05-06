package com.mall.api.controller.member;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.entity.MemberMessage;
import com.mall.service.user.MemberMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Member message controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/message")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberMessageController {

    private final MemberMessageService memberMessageService;

    @GetMapping("/list")
    public R<PageResult<MemberMessage>> list(
            @RequestParam(required = false) Integer msgType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long memberId = UserContext.getUserId();
        return R.ok(memberMessageService.getMessageList(memberId, msgType, page, limit));
    }

    @GetMapping("/unread-count")
    public R<Map<String, Object>> unreadCount() {
        Long memberId = UserContext.getUserId();
        long count = memberMessageService.getUnreadCount(memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", count);
        return R.ok(data);
    }

    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        memberMessageService.markRead(memberId, id);
        return R.ok();
    }
}
