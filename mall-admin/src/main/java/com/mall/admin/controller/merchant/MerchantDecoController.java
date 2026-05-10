package com.mall.admin.controller.merchant;

import com.mall.common.response.PageResult;
import com.mall.common.response.R;
import com.mall.common.util.UserContext;
import com.mall.model.dto.decoration.DecoAlbumDTO;
import com.mall.model.dto.decoration.DecoPageSaveDTO;
import com.mall.model.dto.decoration.DecoTemplateSaveDTO;
import com.mall.model.entity.decoration.DecoAlbum;
import com.mall.model.entity.decoration.DecoImage;
import com.mall.model.entity.decoration.DecoPage;
import com.mall.model.entity.decoration.DecoTemplate;
import com.mall.service.decoration.DecoAlbumService;
import com.mall.service.decoration.DecoPageService;
import com.mall.service.decoration.DecoTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/merchant/deco")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantDecoController {

    private final DecoPageService decoPageService;
    private final DecoAlbumService decoAlbumService;
    private final DecoTemplateService decoTemplateService;

    @GetMapping("/page/list")
    public R<PageResult<DecoPage>> pageList(@RequestParam(required = false) Integer pageType,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer limit) {
        Long tenantId = UserContext.getTenantId();
        return R.ok(decoPageService.list(tenantId, pageType, page, limit));
    }

    @GetMapping("/page/{id}")
    public R<DecoPage> pageDetail(@PathVariable Long id) {
        return R.ok(decoPageService.detail(id));
    }

    @PostMapping("/page/save")
    public R<Void> savePage(@Validated @RequestBody DecoPageSaveDTO dto) {
        Long tenantId = UserContext.getTenantId();
        decoPageService.save(tenantId, dto);
        return R.ok();
    }

    @PutMapping("/page/{id}/publish")
    public R<Void> publishPage(@PathVariable Long id) {
        decoPageService.publish(id);
        return R.ok();
    }

    @DeleteMapping("/page/{id}")
    public R<Void> deletePage(@PathVariable Long id) {
        decoPageService.delete(id);
        return R.ok();
    }

    @GetMapping("/album/list")
    public R<List<DecoAlbum>> albumList() {
        Long tenantId = UserContext.getTenantId();
        return R.ok(decoAlbumService.list(tenantId));
    }

    @PostMapping("/album/create")
    public R<Void> createAlbum(@Validated @RequestBody DecoAlbumDTO dto) {
        Long tenantId = UserContext.getTenantId();
        decoAlbumService.create(tenantId, dto);
        return R.ok();
    }

    @PutMapping("/album/{id}")
    public R<Void> updateAlbum(@PathVariable Long id, @Validated @RequestBody DecoAlbumDTO dto) {
        decoAlbumService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/album/{id}")
    public R<Void> deleteAlbum(@PathVariable Long id) {
        decoAlbumService.delete(id);
        return R.ok();
    }

    @GetMapping("/album/{albumId}/images")
    public R<PageResult<DecoImage>> imageList(@PathVariable Long albumId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(decoAlbumService.images(albumId, page, limit));
    }

    @PostMapping("/album/{albumId}/image")
    public R<DecoImage> uploadImage(@PathVariable Long albumId,
                                     @RequestParam String imageUrl,
                                     @RequestParam(required = false) Integer fileSize,
                                     @RequestParam(required = false) String fileType,
                                     @RequestParam(required = false) String originalName) {
        return R.ok(decoAlbumService.uploadImage(albumId, imageUrl, fileSize, fileType, originalName));
    }

    @DeleteMapping("/image/{imageId}")
    public R<Void> deleteImage(@PathVariable Long imageId) {
        decoAlbumService.deleteImage(imageId);
        return R.ok();
    }

    @GetMapping("/template/list")
    public R<PageResult<DecoTemplate>> templateList(@RequestParam(required = false) Integer pageType,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(decoTemplateService.list(pageType, page, limit));
    }

    @GetMapping("/template/{id}")
    public R<DecoTemplate> templateDetail(@PathVariable Long id) {
        return R.ok(decoTemplateService.detail(id));
    }

    @PostMapping("/template/create")
    public R<Void> createTemplate(@Validated @RequestBody DecoTemplateSaveDTO dto) {
        Long tenantId = UserContext.getTenantId();
        decoTemplateService.create(tenantId, dto);
        return R.ok();
    }
}
