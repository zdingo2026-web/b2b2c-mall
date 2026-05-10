package com.mall.admin.controller.merchant;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.promotion.SeckillActivityCreateDTO;
import com.mall.model.vo.promotion.SeckillActivityDetailVO;
import com.mall.model.vo.promotion.SeckillActivityVO;
import com.mall.service.promotion.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/seckill")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantSeckillController {

    private final SeckillActivityService seckillActivityService;

    @GetMapping("/list")
    public R<PageResult<SeckillActivityVO>> list(@RequestParam(required = false) Integer status,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer limit) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(seckillActivityService.list(tenantId, status, page, limit));
    }

    @GetMapping("/{id}")
    public R<SeckillActivityDetailVO> detail(@PathVariable Long id) {
        return R.ok(seckillActivityService.detail(id));
    }

    @PostMapping("/create")
    public R<Void> create(@Validated @RequestBody SeckillActivityCreateDTO dto) {
        Long tenantId = UserContext.getTenantId();
        seckillActivityService.create(tenantId, dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody SeckillActivityCreateDTO dto) {
        seckillActivityService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        seckillActivityService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/start")
    public R<Void> start(@PathVariable Long id) {
        seckillActivityService.startActivity(id);
        return R.ok();
    }

    @PutMapping("/{id}/end")
    public R<Void> end(@PathVariable Long id) {
        seckillActivityService.endActivity(id);
        return R.ok();
    }
}
