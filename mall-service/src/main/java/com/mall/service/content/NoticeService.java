package com.mall.service.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.dao.mapper.ContentNoticeMapper;
import com.mall.model.entity.ContentNotice;
import com.mall.model.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Notice service.
 * C-end: query valid notices.
 * Admin: list all notices.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final ContentNoticeMapper contentNoticeMapper;

    /**
     * Get notice list for C-end (valid notices only, limited to 10).
     *
     * @return list of NoticeVO
     */
    public List<NoticeVO> getNoticeList() {
        LambdaQueryWrapper<ContentNotice> query = new LambdaQueryWrapper<>();
        query.eq(ContentNotice::getStatus, 1)
                .orderByAsc(ContentNotice::getSort)
                .last("LIMIT 10");

        List<ContentNotice> notices = contentNoticeMapper.selectList(query);
        return notices.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * Get notice list for admin (all notices including disabled).
     *
     * @return list of ContentNotice
     */
    public List<ContentNotice> listAll() {
        LambdaQueryWrapper<ContentNotice> query = new LambdaQueryWrapper<>();
        query.orderByAsc(ContentNotice::getSort);
        return contentNoticeMapper.selectList(query);
    }

    private NoticeVO toVO(ContentNotice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setNoticeType(notice.getNoticeType());
        return vo;
    }
}
