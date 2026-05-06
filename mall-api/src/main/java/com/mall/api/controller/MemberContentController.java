package com.mall.api.controller;

import com.mall.common.response.R;
import com.mall.model.vo.BannerVO;
import com.mall.model.vo.FloorVO;
import com.mall.model.vo.NoticeVO;
import com.mall.service.content.BannerService;
import com.mall.service.content.FloorService;
import com.mall.service.content.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "会员内容")
@RestController
@RequestMapping("/api/v1/member/content")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberContentController {

    private final BannerService bannerService;
    private final FloorService floorService;
    private final NoticeService noticeService;

    @ApiOperation("获取Banner列表")
    @GetMapping("/banner/list")
    public R<List<BannerVO>> getBannerList() {
        List<BannerVO> list = bannerService.getBannerList();
        return R.ok(list);
    }

    @ApiOperation("获取楼层列表")
    @GetMapping("/floor/list")
    public R<List<FloorVO>> getFloorList() {
        List<FloorVO> list = floorService.getFloorList();
        return R.ok(list);
    }

    @ApiOperation("获取公告列表")
    @GetMapping("/notice/list")
    public R<List<NoticeVO>> noticeList() {
        return R.ok(noticeService.getNoticeList());
    }
}
