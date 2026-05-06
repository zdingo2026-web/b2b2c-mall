package com.mall.service.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.ResultCode;
import com.mall.common.util.TenantContext;
import com.mall.dao.mapper.ContentFloorMapper;
import com.mall.model.dto.FloorSaveDTO;
import com.mall.model.entity.ContentFloor;
import com.mall.model.vo.FloorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Floor service.
 * C-end: query valid floors (products loaded asynchronously).
 * Admin: CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FloorService {

    private final ContentFloorMapper contentFloorMapper;

    /**
     * Get floor list for C-end (valid floors only, without products).
     * Products are loaded asynchronously via separate API.
     *
     * @return list of FloorVO (without products)
     */
    public List<FloorVO> getFloorList() {
        LambdaQueryWrapper<ContentFloor> query = new LambdaQueryWrapper<>();
        query.eq(ContentFloor::getStatus, 1)
                .orderByAsc(ContentFloor::getSort);

        List<ContentFloor> floors = contentFloorMapper.selectList(query);
        return floors.stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    /**
     * Get floor by ID (admin).
     *
     * @param id floor ID
     * @return ContentFloor entity
     */
    public ContentFloor getById(Long id) {
        ContentFloor floor = contentFloorMapper.selectById(id);
        if (floor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return floor;
    }

    /**
     * Get floor list for admin (all floors including disabled).
     *
     * @return list of ContentFloor
     */
    public List<ContentFloor> listAll() {
        LambdaQueryWrapper<ContentFloor> query = new LambdaQueryWrapper<>();
        query.orderByAsc(ContentFloor::getSort);
        return contentFloorMapper.selectList(query);
    }

    /**
     * Create a floor (admin).
     *
     * @param dto floor save DTO
     * @return created floor ID
     */
    public Long create(FloorSaveDTO dto) {
        ContentFloor floor = new ContentFloor();
        BeanUtils.copyProperties(dto, floor);
        if (floor.getSort() == null) {
            floor.setSort(0);
        }
        if (floor.getStatus() == null) {
            floor.setStatus(1);
        }
        floor.setTenantId(TenantContext.getTenantId());
        contentFloorMapper.insert(floor);
        return floor.getId();
    }

    /**
     * Update a floor (admin).
     *
     * @param id  floor ID
     * @param dto floor save DTO
     */
    public void update(Long id, FloorSaveDTO dto) {
        ContentFloor floor = contentFloorMapper.selectById(id);
        if (floor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        BeanUtils.copyProperties(dto, floor);
        floor.setId(id);
        contentFloorMapper.updateById(floor);
    }

    /**
     * Delete a floor (admin).
     *
     * @param id floor ID
     */
    public void delete(Long id) {
        ContentFloor floor = contentFloorMapper.selectById(id);
        if (floor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        contentFloorMapper.deleteById(id);
    }

    private FloorVO toSimpleVO(ContentFloor floor) {
        FloorVO vo = new FloorVO();
        vo.setId(floor.getId());
        vo.setFloorName(floor.getFloorName());
        vo.setStyle(floor.getStyle());
        // Products are not loaded here; they are loaded asynchronously
        return vo;
    }
}
