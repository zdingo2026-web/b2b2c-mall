package com.mall.service.content;

import com.mall.model.vo.BannerVO;
import com.mall.model.vo.FloorVO;
import com.mall.model.vo.HomeVO;
import com.mall.model.vo.NoticeVO;
import com.mall.model.vo.QuickEntryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Home page aggregation service.
 * Aggregates Banner, Floor, and new product data for the home page.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final BannerService bannerService;
    private final FloorService floorService;
    private final NoticeService noticeService;

    /**
     * Get home page data: banners + floor framework + new product recommendations.
     *
     * @return HomeVO with aggregated data
     */
    public HomeVO getHomeData() {
        HomeVO homeVO = new HomeVO();

        // Load banners
        List<BannerVO> banners = bannerService.getBannerList();
        homeVO.setBanners(banners);

        // Load floor framework (without products, products loaded async)
        List<FloorVO> floors = floorService.getFloorList();
        List<HomeVO.FloorSimpleVO> simpleFloors = floors.stream().map(f -> {
            HomeVO.FloorSimpleVO simple = new HomeVO.FloorSimpleVO();
            simple.setId(f.getId());
            simple.setFloorName(f.getFloorName());
            simple.setStyle(f.getStyle());
            return simple;
        }).collect(Collectors.toList());
        homeVO.setFloors(simpleFloors);

        // New products (MVP: empty list, loaded from product service)
        homeVO.setNewProducts(Collections.emptyList());

        // Load notices
        List<NoticeVO> notices = noticeService.getNoticeList();
        homeVO.setNotices(notices);

        // Load quick entries (hardcoded 10 items matching design grid)
        List<QuickEntryVO> quickEntries = buildQuickEntries();
        homeVO.setQuickEntries(quickEntries);

        return homeVO;
    }

    /**
     * Build hardcoded quick entry items for the home page grid (10 items).
     */
    private List<QuickEntryVO> buildQuickEntries() {
        List<QuickEntryVO> entries = new ArrayList<>();

        entries.add(buildEntry("分类", "ri-apps-line", "#FF6B6B", "#FFFFFF", "category", "/pages/category/index"));
        entries.add(buildEntry("限时抢购", "ri-flashlight-line", "#FF9F43", "#FFFFFF", "flashsale", "/pages/flash-sale/index"));
        entries.add(buildEntry("领券中心", "ri-coupon-3-line", "#54A0FF", "#FFFFFF", "coupon", "/pages/coupon/index"));
        entries.add(buildEntry("拼团", "ri-group-line", "#5F27CD", "#FFFFFF", "groupbuy", "/pages/group-buy/index"));
        entries.add(buildEntry("积分商城", "ri-coin-line", "#01A3A4", "#FFFFFF", "points", "/pages/points/index"));
        entries.add(buildEntry("新品首发", "ri-seedling-line", "#10AC84", "#FFFFFF", "new", "/pages/new-product/index"));
        entries.add(buildEntry("品牌特卖", "ri-store-2-line", "#EE5A24", "#FFFFFF", "brand", "/pages/brand/index"));
        entries.add(buildEntry("会员中心", "ri-vip-crown-line", "#C44569", "#FFFFFF", "vip", "/pages/vip/index"));
        entries.add(buildEntry("预售", "ri-time-line", "#6C5CE7", "#FFFFFF", "presale", "/pages/presale/index"));
        entries.add(buildEntry("更多", "ri-more-line", "#8395A7", "#FFFFFF", "more", "/pages/more/index"));

        return entries;
    }

    private QuickEntryVO buildEntry(String name, String icon, String bgColor, String iconColor, String linkType, String linkUrl) {
        QuickEntryVO vo = new QuickEntryVO();
        vo.setName(name);
        vo.setIcon(icon);
        vo.setBgColor(bgColor);
        vo.setIconColor(iconColor);
        vo.setLinkType(linkType);
        vo.setLinkUrl(linkUrl);
        return vo;
    }
}
