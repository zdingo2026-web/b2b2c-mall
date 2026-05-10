package com.mall.api.controller.member;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.promotion.GroupBuyJoinDTO;
import com.mall.model.vo.promotion.GroupBuyDetailVO;
import com.mall.model.vo.promotion.GroupBuyProductVO;
import com.mall.model.vo.promotion.GroupBuyRecordVO;
import com.mall.service.promotion.GroupBuyActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/member/group-buy")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberGroupBuyController {

    private final GroupBuyActivityService groupBuyActivityService;

    @GetMapping("/products")
    public R<PageResult<GroupBuyProductVO>> products(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(groupBuyActivityService.products(page, limit));
    }

    @GetMapping("/{activityId}")
    public R<GroupBuyDetailVO> groupDetail(@PathVariable Long activityId) {
        return R.ok(groupBuyActivityService.groupDetail(activityId));
    }

    @PostMapping("/{activityId}/join")
    public R<GroupBuyRecordVO> joinGroup(@PathVariable Long activityId, @Validated @RequestBody GroupBuyJoinDTO dto) {
        Long memberId = UserContext.getUserId();
        return R.ok(groupBuyActivityService.joinGroup(memberId, activityId, dto));
    }
}
