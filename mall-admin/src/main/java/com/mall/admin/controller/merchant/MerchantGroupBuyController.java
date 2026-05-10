package com.mall.admin.controller.merchant;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.promotion.GroupBuyCreateDTO;
import com.mall.model.vo.promotion.GroupBuyActivityDetailVO;
import com.mall.model.vo.promotion.GroupBuyActivityVO;
import com.mall.service.promotion.GroupBuyActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/group-buy")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantGroupBuyController {

    private final GroupBuyActivityService groupBuyActivityService;

    @GetMapping("/list")
    public R<PageResult<GroupBuyActivityVO>> list(@RequestParam(required = false) Integer status,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer limit) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(groupBuyActivityService.list(tenantId, status, page, limit));
    }

    @GetMapping("/{id}")
    public R<GroupBuyActivityDetailVO> detail(@PathVariable Long id) {
        return R.ok(groupBuyActivityService.detail(id));
    }

    @PostMapping("/create")
    public R<Void> create(@Validated @RequestBody GroupBuyCreateDTO dto) {
        Long tenantId = UserContext.getTenantId();
        groupBuyActivityService.create(tenantId, dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody GroupBuyCreateDTO dto) {
        groupBuyActivityService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        groupBuyActivityService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/start")
    public R<Void> start(@PathVariable Long id) {
        groupBuyActivityService.startActivity(id);
        return R.ok();
    }

    @PutMapping("/{id}/end")
    public R<Void> end(@PathVariable Long id) {
        groupBuyActivityService.endActivity(id);
        return R.ok();
    }
}
