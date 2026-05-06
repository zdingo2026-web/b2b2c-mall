package com.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 首页聚合数据 VO
 */
@Data
public class HomeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Banner列表 */
    private List<BannerVO> banners;

    /** 楼层列表(不含商品，商品异步加载) */
    private List<FloorSimpleVO> floors;

    /** 新品推荐 */
    private List<FloorVO.SpuVO> newProducts;

    /** 公告列表 */
    private List<NoticeVO> notices;

    /** 快捷入口列表 */
    private List<QuickEntryVO> quickEntries;

    /**
     * 楼层简要信息(不含商品列表)
     */
    @Data
    public static class FloorSimpleVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;

        /** 楼层名称 */
        private String floorName;

        /** 展示样式 */
        private Integer style;
    }
}
