package com.mall.api.controller.member;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.vo.promotion.SeckillProductVO;
import com.mall.model.vo.promotion.SeckillResultVO;
import com.mall.model.vo.promotion.SeckillTimeSlotVO;
import com.mall.service.promotion.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/member/seckill")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberSeckillController {

    private final SeckillActivityService seckillActivityService;

    @GetMapping("/time-slots")
    public R<List<SeckillTimeSlotVO>> timeSlots() {
        return R.ok(seckillActivityService.timeSlots());
    }

    @GetMapping("/products")
    public R<PageResult<SeckillProductVO>> products(
            @RequestParam Long activityId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(seckillActivityService.products(activityId, page, limit));
    }

    @PostMapping("/{activityId}/seckill")
    public R<SeckillResultVO> seckill(@PathVariable Long activityId, @RequestParam Long skuId) {
        Long memberId = UserContext.getUserId();
        return R.ok(seckillActivityService.seckill(memberId, activityId, skuId));
    }

    @GetMapping("/result")
    public R<SeckillResultVO> getSeckillResult(@RequestParam Long activityId, @RequestParam Long skuId) {
        Long memberId = UserContext.getUserId();
        return R.ok(seckillActivityService.getSeckillResult(memberId, activityId, skuId));
    }
}
