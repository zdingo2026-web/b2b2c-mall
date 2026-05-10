package com.mall.admin.controller.platform;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.model.dto.promotion.CouponTemplateCreateDTO;
import com.mall.model.vo.promotion.CouponTemplateVO;
import com.mall.service.promotion.CouponTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform/coupon")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformCouponController {

    private final CouponTemplateService couponTemplateService;

    @GetMapping("/list")
    public R<PageResult<CouponTemplateVO>> list(
            @RequestParam(required = false) Integer couponType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(couponTemplateService.list(null, couponType, status, page, limit));
    }

    @GetMapping("/{id}")
    public R<CouponTemplateVO> detail(@PathVariable Long id) {
        return R.ok(couponTemplateService.detail(id));
    }

    @PostMapping("/create")
    public R<Void> create(@Validated @RequestBody CouponTemplateCreateDTO dto) {
        couponTemplateService.create(0L, dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody CouponTemplateCreateDTO dto) {
        couponTemplateService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        couponTemplateService.delete(id);
        return R.ok();
    }
}
