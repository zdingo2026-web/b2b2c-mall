package com.mall.admin.controller.platform;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.model.entity.ContentNotice;
import com.mall.service.content.NoticeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.dao.mapper.ContentNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform/content/notice")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM')")
public class PlatformContentController {

    private final ContentNoticeMapper contentNoticeMapper;

    @GetMapping("/list")
    public R<PageResult<ContentNotice>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Page<ContentNotice> pageParam = new Page<>(page, limit);
        Page<ContentNotice> result = contentNoticeMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ContentNotice>().orderByAsc(ContentNotice::getSort));
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, limit));
    }

    @GetMapping("/{id}")
    public R<ContentNotice> detail(@PathVariable Long id) {
        return R.ok(contentNoticeMapper.selectById(id));
    }

    @PostMapping("/create")
    public R<Long> create(@RequestBody ContentNotice notice) {
        contentNoticeMapper.insert(notice);
        return R.ok(notice.getId());
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody ContentNotice dto) {
        ContentNotice existing = contentNoticeMapper.selectById(id);
        if (existing == null) {
            return R.fail("公告不存在");
        }
        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getNoticeType() != null) existing.setNoticeType(dto.getNoticeType());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getSort() != null) existing.setSort(dto.getSort());
        contentNoticeMapper.updateById(existing);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        contentNoticeMapper.deleteById(id);
        return R.ok();
    }
}
