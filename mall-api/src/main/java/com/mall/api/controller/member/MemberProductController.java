package com.mall.api.controller.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.dao.mapper.ProductBrandMapper;
import com.mall.dao.mapper.ProductCommentMapper;
import com.mall.model.dto.CommentCreateDTO;
import com.mall.model.dto.SpuQueryDTO;
import com.mall.model.entity.ProductBrand;
import com.mall.model.entity.ProductComment;
import com.mall.model.vo.CategoryTreeVO;
import com.mall.model.vo.CommentSummaryVO;
import com.mall.model.vo.SpuDetailVO;
import com.mall.model.vo.SpuVO;
import com.mall.service.product.ProductCategoryService;
import com.mall.service.product.ProductSpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Member product browsing controller (C-end).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member/product")
@RequiredArgsConstructor
public class MemberProductController {

    private final ProductSpuService productSpuService;
    private final ProductCategoryService productCategoryService;
    private final ProductBrandMapper productBrandMapper;
    private final ProductCommentMapper productCommentMapper;

    @GetMapping("/spu/list")
    public R<PageResult<SpuVO>> spuList(SpuQueryDTO dto) {
        // C-end: only show published products
        dto.setStatus(1);
        return R.ok(productSpuService.getSpuList(dto, null));
    }

    @GetMapping("/spu/{id}")
    public R<SpuDetailVO> spuDetail(@PathVariable Long id) {
        return R.ok(productSpuService.getSpuDetail(id));
    }

    @GetMapping("/category/tree")
    public R<List<CategoryTreeVO>> categoryTree(
            @RequestParam(value = "tenantId", required = false, defaultValue = "0") Long tenantId) {
        return R.ok(productCategoryService.getCategoryTree(tenantId));
    }

    @GetMapping("/brand/list")
    public R<List<ProductBrand>> brandList() {
        List<ProductBrand> brands = productBrandMapper.selectList(
                new LambdaQueryWrapper<ProductBrand>()
                        .eq(ProductBrand::getStatus, 1)
                        .orderByAsc(ProductBrand::getSortOrder));
        return R.ok(brands);
    }

    @GetMapping("/spu/{id}/comment-summary")
    public R<CommentSummaryVO> commentSummary(@PathVariable Long id) {
        // Total count
        long totalCount = productCommentMapper.selectCount(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getSpuId, id)
                        .eq(ProductComment::getStatus, 1));

        // Good count (score >= 4)
        long goodCount = productCommentMapper.selectCount(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getSpuId, id)
                        .eq(ProductComment::getStatus, 1)
                        .ge(ProductComment::getScore, 4));

        double goodRate = totalCount > 0 ? (double) goodCount / totalCount : 0.0;

        CommentSummaryVO vo = new CommentSummaryVO();
        vo.setTotalCount((int) totalCount);
        vo.setGoodRate(Math.round(goodRate * 100.0) / 100.0);
        vo.setTags(new ArrayList<>());
        return R.ok(vo);
    }

    @GetMapping("/comment/list")
    public R<PageResult<ProductComment>> commentList(
            @RequestParam Long spuId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Page<ProductComment> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<ProductComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductComment::getSpuId, spuId)
                .eq(ProductComment::getStatus, 1)
                .orderByDesc(ProductComment::getCreateTime);
        Page<ProductComment> result = productCommentMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, limit));
    }

    /**
     * Submit a product review (C-11).
     * Requires authenticated member.
     */
    @PostMapping("/comment")
    @PreAuthorize("hasRole('MEMBER')")
    public R<Long> submitComment(@Validated @RequestBody CommentCreateDTO dto) {
        Long memberId = UserContext.getUserId();
        return R.ok(productSpuService.submitComment(memberId, dto));
    }
}
