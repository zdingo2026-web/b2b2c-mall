package com.mall.service.decoration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.PageResult;
import com.mall.common.response.ResultCode;
import com.mall.dao.mapper.decoration.DecoAlbumMapper;
import com.mall.dao.mapper.decoration.DecoImageMapper;
import com.mall.model.dto.decoration.DecoAlbumDTO;
import com.mall.model.entity.decoration.DecoAlbum;
import com.mall.model.entity.decoration.DecoImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecoAlbumService {

    private final DecoAlbumMapper decoAlbumMapper;
    private final DecoImageMapper decoImageMapper;

    public List<DecoAlbum> list(Long tenantId) {
        LambdaQueryWrapper<DecoAlbum> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(DecoAlbum::getTenantId, tenantId);
        }
        wrapper.orderByAsc(DecoAlbum::getSortOrder);
        return decoAlbumMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long tenantId, DecoAlbumDTO dto) {
        DecoAlbum album = new DecoAlbum();
        album.setTenantId(tenantId);
        album.setAlbumName(dto.getAlbumName());
        album.setSortOrder(dto.getSortOrder());
        album.setImageCount(0);
        decoAlbumMapper.insert(album);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DecoAlbumDTO dto) {
        DecoAlbum album = decoAlbumMapper.selectById(id);
        if (album == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        album.setAlbumName(dto.getAlbumName());
        album.setSortOrder(dto.getSortOrder());
        decoAlbumMapper.updateById(album);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DecoAlbum album = decoAlbumMapper.selectById(id);
        if (album == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        decoImageMapper.delete(
                new LambdaQueryWrapper<DecoImage>().eq(DecoImage::getAlbumId, id));
        decoAlbumMapper.deleteById(id);
    }

    public PageResult<DecoImage> images(Long albumId, int page, int limit) {
        Page<DecoImage> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<DecoImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DecoImage::getAlbumId, albumId)
                .orderByDesc(DecoImage::getCreateTime);
        Page<DecoImage> result = decoImageMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, limit);
    }

    @Transactional(rollbackFor = Exception.class)
    public DecoImage uploadImage(Long albumId, String imageUrl, Integer fileSize, String fileType, String originalName) {
        DecoAlbum album = decoAlbumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        DecoImage image = new DecoImage();
        image.setAlbumId(albumId);
        image.setImageUrl(imageUrl);
        image.setFileSize(fileSize);
        image.setFileType(fileType);
        image.setOriginalName(originalName);
        image.setTenantId(album.getTenantId());
        decoImageMapper.insert(image);

        album.setImageCount(album.getImageCount() != null ? album.getImageCount() + 1 : 1);
        decoAlbumMapper.updateById(album);

        return image;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(Long imageId) {
        DecoImage image = decoImageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        decoImageMapper.deleteById(imageId);

        DecoAlbum album = decoAlbumMapper.selectById(image.getAlbumId());
        if (album != null && album.getImageCount() != null && album.getImageCount() > 0) {
            album.setImageCount(album.getImageCount() - 1);
            decoAlbumMapper.updateById(album);
        }
    }
}
