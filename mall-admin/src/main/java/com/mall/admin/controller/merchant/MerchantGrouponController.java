package com.mall.admin.controller.merchant;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.promotion.GrouponCreateDTO;
import com.mall.model.vo.promotion.GrouponActivityVO;
import com.mall.service.promotion.GrouponActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/groupon")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantGrouponController {

    private final GrouponActivityService grouponActivityService;

    @GetMapping("/list")
    public R<PageResult<GrouponActivityVO>> list(@RequestParam(required = false) Integer status,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer limit) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(grouponActivityService.list(tenantId, status, page, limit));
    }

    @PostMapping("/create")
    public R<Void> create(@Validated @RequestBody GrouponCreateDTO dto) {
        Long tenantId = UserContext.getTenantId();
        grouponActivityService.create(tenantId, dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody GrouponCreateDTO dto) {
        grouponActivityService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        grouponActivityService.delete(id);
        return R.ok();
    }
}
