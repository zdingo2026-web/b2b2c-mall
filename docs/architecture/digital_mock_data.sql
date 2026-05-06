-- ============================================================
-- 商户 digital (tenant_id=2, 电子数码旗舰店) 完整模拟数据
-- 覆盖模块：商品SPU/SKU、订单、子管理员、结算记录、退款记录
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. 品牌数据（数码相关品牌，供商品引用）
-- ============================================================
INSERT INTO `product_brand` (`tenant_id`, `brand_name`, `logo`, `description`, `sort_order`, `status`, `deleted`)
VALUES
(0, '华为', '', '华为品牌', 100, 1, 0),
(0, '苹果', '', 'Apple品牌', 99, 1, 0),
(0, '小米', '', '小米品牌', 98, 1, 0),
(0, '联想', '', '联想品牌', 97, 1, 0),
(0, '索尼', '', 'Sony品牌', 96, 1, 0);

SET @huawei_id = LAST_INSERT_ID();
SET @apple_id = @huawei_id + 1;
SET @xiaomi_id = @huawei_id + 2;
SET @lenovo_id = @huawei_id + 3;
SET @sony_id = @huawei_id + 4;

-- ============================================================
-- 2. 商品SPU（12个商品，覆盖多种状态和分类）
-- ============================================================
INSERT INTO `product_spu` (`tenant_id`, `category_id`, `brand_id`, `product_name`, `sub_title`, `main_image`, `images`, `description`, `min_price`, `max_price`, `total_sales`, `total_stock`, `status`, `audit_status`, `tag_type`, `original_price`, `sort_order`, `deleted`)
VALUES
-- 已上架商品 (status=2)
(2, 36, @huawei_id, '华为Mate 60 Pro', '卫星通信旗舰手机', '/images/product/huawei-mate60.jpg', '/images/product/huawei-mate60.jpg', '<p>华为Mate 60 Pro，搭载麒麟芯片，支持卫星通信</p>', 6999.00, 7999.00, 328, 500, 2, 0, 1, 7999.00, 100, 0),
(2, 36, @apple_id, 'iPhone 15 Pro Max', 'A17 Pro芯片 钛金属', '/images/product/iphone15pro.jpg', '/images/product/iphone15pro.jpg', '<p>iPhone 15 Pro Max，钛金属设计，A17 Pro芯片</p>', 9999.00, 13999.00, 512, 300, 2, 0, 2, 11999.00, 99, 0),
(2, 38, @xiaomi_id, '红魔9 Pro', '电竞旗舰 性能怪兽', '/images/product/redmagic9.jpg', '/images/product/redmagic9.jpg', '<p>红魔9 Pro游戏手机，骁龙8 Gen3</p>', 4399.00, 5999.00, 156, 200, 2, 0, 4, 5999.00, 98, 0),
(2, 39, @huawei_id, '华为MatePad Pro 13.2', '星闪连接 超清大屏', '/images/product/matepad-pro.jpg', '/images/product/matepad-pro.jpg', '<p>华为MatePad Pro 13.2英寸，星闪连接</p>', 5199.00, 6999.00, 89, 150, 2, 0, 3, 6999.00, 97, 0),
(2, 40, @apple_id, 'iPad Pro M4', 'M4芯片 超薄设计', '/images/product/ipad-pro-m4.jpg', '/images/product/ipad-pro-m4.jpg', '<p>iPad Pro M4芯片，超薄设计</p>', 8999.00, 14999.00, 203, 250, 2, 0, 2, 12999.00, 96, 0),
(2, 42, @apple_id, 'Apple Watch Ultra 2', '极限运动 精准双频GPS', '/images/product/apple-watch-ultra2.jpg', '/images/product/apple-watch-ultra2.jpg', '<p>Apple Watch Ultra 2，钛金属表壳</p>', 6499.00, 6999.00, 67, 100, 2, 0, 0, 6999.00, 95, 0),
(2, 45, @sony_id, '索尼WH-1000XM5', '行业领先降噪 沉浸音质', '/images/product/sony-xm5.jpg', '/images/product/sony-xm5.jpg', '<p>索尼WH-1000XM5头戴式降噪耳机</p>', 2499.00, 2999.00, 445, 600, 2, 0, 0, 2999.00, 94, 0),
(2, 16, @lenovo_id, '联想ThinkPad X1 Carbon', '商务旗舰 超轻薄', '/images/product/thinkpad-x1.jpg', '/images/product/thinkpad-x1.jpg', '<p>ThinkPad X1 Carbon Gen 11</p>', 11999.00, 15999.00, 78, 80, 2, 0, 2, 15999.00, 93, 0),

-- 草稿商品 (status=0)
(2, 37, @xiaomi_id, '红米Note 13 Pro', '2亿像素 超清主摄', '/images/product/redmi-note13.jpg', '/images/product/redmi-note13.jpg', '<p>红米Note 13 Pro，2亿像素</p>', 1499.00, 1999.00, 0, 1000, 0, 0, 3, 1999.00, 90, 0),
(2, 46, @sony_id, '索尼IER-Z1R', '入耳式旗舰耳机', '/images/product/sony-ierz1r.jpg', '/images/product/sony-ierz1r.jpg', '<p>索尼IER-Z1R入耳式耳机</p>', 8999.00, 8999.00, 0, 50, 0, 0, 0, 9999.00, 89, 0),

-- 已下架商品 (status=3)
(2, 36, @huawei_id, '华为P60 Pro', '超聚光夜视 长焦微距', '/images/product/huawei-p60.jpg', '/images/product/huawei-p60.jpg', '<p>华为P60 Pro，超聚光影像</p>', 5988.00, 6988.00, 1024, 0, 3, 0, 0, 6988.00, 88, 0),
(2, 43, @xiaomi_id, '小米手环8 Pro', '大屏独立定位 专业运动', '/images/product/mi-band8pro.jpg', '/images/product/mi-band8pro.jpg', '<p>小米手环8 Pro</p>', 299.00, 349.00, 2560, 0, 3, 0, 0, 349.00, 87, 0);

-- 记录SPU的ID范围
SET @spu_start = LAST_INSERT_ID();
SET @spu_huawei_mate60 = @spu_start;
SET @spu_iphone15pro = @spu_start + 1;
SET @spu_redmagic9 = @spu_start + 2;
SET @spu_matepad_pro = @spu_start + 3;
SET @spu_ipad_pro_m4 = @spu_start + 4;
SET @spu_apple_watch = @spu_start + 5;
SET @spu_sony_xm5 = @spu_start + 6;
SET @spu_thinkpad_x1 = @spu_start + 7;
SET @spu_redmi_note13 = @spu_start + 8;
SET @spu_sony_ier = @spu_start + 9;
SET @spu_huawei_p60 = @spu_start + 10;
SET @spu_mi_band8 = @spu_start + 11;

-- ============================================================
-- 3. 商品SKU（每个SPU 2-3个规格）
-- ============================================================
INSERT INTO `product_sku` (`tenant_id`, `spu_id`, `sku_name`, `sku_code`, `image`, `price`, `original_price`, `stock`, `lock_stock`, `sales`, `spec_values`, `weight`, `status`, `deleted`)
VALUES
-- 华为Mate 60 Pro (3个SKU)
(2, @spu_huawei_mate60, '华为Mate 60 Pro 256GB 雅丹黑', 'HW-MATE60-256-BK', '/images/product/huawei-mate60.jpg', 6999.00, 7999.00, 200, 0, 128, '{"颜色":"雅丹黑","存储":"256GB"}', 0.21, 1, 0),
(2, @spu_huawei_mate60, '华为Mate 60 Pro 512GB 雅丹黑', 'HW-MATE60-512-BK', '/images/product/huawei-mate60.jpg', 7499.00, 8499.00, 150, 0, 100, '{"颜色":"雅丹黑","存储":"512GB"}', 0.21, 1, 0),
(2, @spu_huawei_mate60, '华为Mate 60 Pro 512GB 南糯紫', 'HW-MATE60-512-PU', '/images/product/huawei-mate60.jpg', 7999.00, 8999.00, 150, 0, 100, '{"颜色":"南糯紫","存储":"512GB"}', 0.21, 1, 0),

-- iPhone 15 Pro Max (3个SKU)
(2, @spu_iphone15pro, 'iPhone 15 Pro Max 256GB 原色钛金属', 'AP-IP15PM-256-NAT', '/images/product/iphone15pro.jpg', 9999.00, 10999.00, 100, 0, 180, '{"颜色":"原色钛金属","存储":"256GB"}', 0.22, 1, 0),
(2, @spu_iphone15pro, 'iPhone 15 Pro Max 512GB 蓝色钛金属', 'AP-IP15PM-512-BLU', '/images/product/iphone15pro.jpg', 11499.00, 12499.00, 100, 0, 172, '{"颜色":"蓝色钛金属","存储":"512GB"}', 0.22, 1, 0),
(2, @spu_iphone15pro, 'iPhone 15 Pro Max 1TB 白色钛金属', 'AP-IP15PM-1T-WHT', '/images/product/iphone15pro.jpg', 13999.00, 14999.00, 100, 0, 160, '{"颜色":"白色钛金属","存储":"1TB"}', 0.22, 1, 0),

-- 红魔9 Pro (2个SKU)
(2, @spu_redmagic9, '红魔9 Pro 12+256GB 暗夜骑士', 'RM-9PRO-12-256-BK', '/images/product/redmagic9.jpg', 4399.00, 5999.00, 100, 0, 80, '{"颜色":"暗夜骑士","存储":"12+256GB"}', 0.23, 1, 0),
(2, @spu_redmagic9, '红魔9 Pro 16+512GB 氘锋透明', 'RM-9PRO-16-512-TR', '/images/product/redmagic9.jpg', 5999.00, 6999.00, 100, 0, 76, '{"颜色":"氘锋透明","存储":"16+512GB"}', 0.23, 1, 0),

-- 华为MatePad Pro (2个SKU)
(2, @spu_matepad_pro, 'MatePad Pro 13.2 12+256GB 曜金黑', 'HW-MATEPAD-12-256', '/images/product/matepad-pro.jpg', 5199.00, 6999.00, 80, 0, 45, '{"颜色":"曜金黑","存储":"12+256GB"}', 0.58, 1, 0),
(2, @spu_matepad_pro, 'MatePad Pro 13.2 16+512GB 曜金黑', 'HW-MATEPAD-16-512', '/images/product/matepad-pro.jpg', 6999.00, 7999.00, 70, 0, 44, '{"颜色":"曜金黑","存储":"16+512GB"}', 0.58, 1, 0),

-- iPad Pro M4 (2个SKU)
(2, @spu_ipad_pro_m4, 'iPad Pro M4 256GB 银色', 'AP-IPADPM4-256-SLV', '/images/product/ipad-pro-m4.jpg', 8999.00, 9999.00, 120, 0, 105, '{"颜色":"银色","存储":"256GB"}', 0.44, 1, 0),
(2, @spu_ipad_pro_m4, 'iPad Pro M4 1TB 深空黑色', 'AP-IPADPM4-1T-SPB', '/images/product/ipad-pro-m4.jpg', 14999.00, 16999.00, 130, 0, 98, '{"颜色":"深空黑色","存储":"1TB"}', 0.44, 1, 0),

-- Apple Watch Ultra 2 (2个SKU)
(2, @spu_apple_watch, 'Apple Watch Ultra 2 49mm 钛金属', 'AP-AWU2-49-TI', '/images/product/apple-watch-ultra2.jpg', 6499.00, 6999.00, 50, 0, 35, '{"尺寸":"49mm","材质":"钛金属"}', 0.06, 1, 0),
(2, @spu_apple_watch, 'Apple Watch Ultra 2 49mm 黑色钛金属', 'AP-AWU2-49-BKT', '/images/product/apple-watch-ultra2.jpg', 6999.00, 7499.00, 50, 0, 32, '{"尺寸":"49mm","材质":"黑色钛金属"}', 0.06, 1, 0),

-- 索尼WH-1000XM5 (2个SKU)
(2, @spu_sony_xm5, 'WH-1000XM5 黑色', 'SN-XM5-BK', '/images/product/sony-xm5.jpg', 2499.00, 2999.00, 300, 0, 230, '{"颜色":"黑色"}', 0.25, 1, 0),
(2, @spu_sony_xm5, 'WH-1000XM5 铂金银', 'SN-XM5-SLV', '/images/product/sony-xm5.jpg', 2999.00, 3499.00, 300, 0, 215, '{"颜色":"铂金银"}', 0.25, 1, 0),

-- ThinkPad X1 Carbon (2个SKU)
(2, @spu_thinkpad_x1, 'X1 Carbon i7/16G/512G 黑色', 'LN-X1C-I7-16-512', '/images/product/thinkpad-x1.jpg', 11999.00, 13999.00, 40, 0, 40, '{"处理器":"i7-1365U","内存":"16GB","硬盘":"512GB"}', 1.12, 1, 0),
(2, @spu_thinkpad_x1, 'X1 Carbon i7/32G/1T 黑色', 'LN-X1C-I7-32-1T', '/images/product/thinkpad-x1.jpg', 15999.00, 18999.00, 40, 0, 38, '{"处理器":"i7-1365U","内存":"32GB","硬盘":"1TB"}', 1.12, 1, 0),

-- 红米Note 13 Pro 草稿 (2个SKU)
(2, @spu_redmi_note13, '红米Note 13 Pro 8+128GB 星沙白', 'XM-RN13P-8-128-WH', '/images/product/redmi-note13.jpg', 1499.00, 1699.00, 500, 0, 0, '{"颜色":"星沙白","存储":"8+128GB"}', 0.19, 1, 0),
(2, @spu_redmi_note13, '红米Note 13 Pro 12+256GB 子夜黑', 'XM-RN13P-12-256-BK', '/images/product/redmi-note13.jpg', 1999.00, 2299.00, 500, 0, 0, '{"颜色":"子夜黑","存储":"12+256GB"}', 0.19, 1, 0),

-- 索尼IER-Z1R 草稿 (1个SKU)
(2, @spu_sony_ier, 'IER-Z1R 入耳式耳机', 'SN-IERZ1R', '/images/product/sony-ierz1r.jpg', 8999.00, 9999.00, 50, 0, 0, '{"颜色":"黑色"}', 0.03, 1, 0),

-- 华为P60 Pro 已下架 (2个SKU)
(2, @spu_huawei_p60, '华为P60 Pro 256GB 翡翠绿', 'HW-P60-256-GR', '/images/product/huawei-p60.jpg', 5988.00, 6988.00, 0, 0, 520, '{"颜色":"翡翠绿","存储":"256GB"}', 0.20, 1, 0),
(2, @spu_huawei_p60, '华为P60 Pro 512GB 羽砂紫', 'HW-P60-512-PU', '/images/product/huawei-p60.jpg', 6988.00, 7988.00, 0, 0, 504, '{"颜色":"羽砂紫","存储":"512GB"}', 0.20, 0, 0),

-- 小米手环8 Pro 已下架 (2个SKU)
(2, @spu_mi_band8, '小米手环8 Pro 黑色', 'XM-BAND8P-BK', '/images/product/mi-band8pro.jpg', 299.00, 349.00, 0, 0, 1300, '{"颜色":"黑色"}', 0.03, 0, 0),
(2, @spu_mi_band8, '小米手环8 Pro 象牙白', 'XM-BAND8P-WH', '/images/product/mi-band8pro.jpg', 349.00, 399.00, 0, 0, 1260, '{"颜色":"象牙白"}', 0.03, 0, 0);

-- 记录SKU的ID范围
SET @sku_start = LAST_INSERT_ID();
-- SKU顺序: mate60*3, iphone15*3, redmagic*2, matepad*2, ipad*2, watch*2, sony_xm5*2, thinkpad*2, redmi*2, sony_ier*1, p60*2, miband*2
-- 索引偏移 (从 @sku_start 开始):
-- 0-2: mate60, 3-5: iphone15, 6-7: redmagic, 8-9: matepad, 10-11: ipad, 12-13: watch, 14-15: sony_xm5, 16-17: thinkpad

SET @sku_mate60_256 = @sku_start;
SET @sku_mate60_512_bk = @sku_start + 1;
SET @sku_mate60_512_pu = @sku_start + 2;
SET @sku_iphone_256 = @sku_start + 3;
SET @sku_iphone_512 = @sku_start + 4;
SET @sku_iphone_1t = @sku_start + 5;
SET @sku_redmagic_256 = @sku_start + 6;
SET @sku_redmagic_512 = @sku_start + 7;
SET @sku_matepad_256 = @sku_start + 8;
SET @sku_matepad_512 = @sku_start + 9;
SET @sku_ipad_256 = @sku_start + 10;
SET @sku_ipad_1t = @sku_start + 11;
SET @sku_watch_ti = @sku_start + 12;
SET @sku_watch_bkt = @sku_start + 13;
SET @sku_sony_xm5_bk = @sku_start + 14;
SET @sku_sony_xm5_slv = @sku_start + 15;
SET @sku_thinkpad_16 = @sku_start + 16;
SET @sku_thinkpad_32 = @sku_start + 17;

-- ============================================================
-- 4. 订单主表 (15个订单，覆盖各种状态)
-- ============================================================
INSERT INTO `order_main` (`tenant_id`, `order_no`, `parent_id`, `order_type`, `member_id`, `total_amount`, `freight_amount`, `discount_amount`, `pay_amount`, `pay_type`, `pay_time`, `order_status`, `pay_status`, `delivery_type`, `logistics_no`, `logistics_company`, `delivery_time`, `receive_time`, `remark`, `expire_time`, `version`, `tenant_name`, `is_reviewed`, `deleted`)
VALUES
-- 待付款 (order_status=0)
(2, 'DG20260504001', 0, 1, 2049786354276929537, 6999.00, 0.00, 0.00, 6999.00, 1, NULL, 0, 0, 1, NULL, NULL, NULL, NULL, '请尽快发货', DATE_ADD(NOW(), INTERVAL 30 MINUTE), 1, '电子数码旗舰店', 0, 0),
(2, 'DG20260504002', 0, 1, 2049790077195689985, 2499.00, 0.00, 0.00, 2499.00, 2, NULL, 0, 0, 1, NULL, NULL, NULL, NULL, '', DATE_ADD(NOW(), INTERVAL 30 MINUTE), 1, '电子数码旗舰店', 0, 0),

-- 待发货 (order_status=1)
(2, 'DG20260503001', 0, 1, 2049795525823094785, 9999.00, 0.00, 100.00, 9899.00, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), 1, 1, 1, NULL, NULL, NULL, NULL, '加急', NULL, 1, '电子数码旗舰店', 0, 0),
(2, 'DG20260503002', 0, 1, 2049797085860950018, 6999.00, 0.00, 0.00, 6999.00, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR), 1, 1, 1, NULL, NULL, NULL, NULL, '', NULL, 1, '电子数码旗舰店', 0, 0),
(2, 'DG20260502001', 0, 1, 2049797085860950019, 13999.00, 0.00, 200.00, 13799.00, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR), 1, 1, 1, NULL, NULL, NULL, NULL, '请包装好', NULL, 1, '电子数码旗舰店', 0, 0),

-- 待收货 (order_status=2)
(2, 'DG20260501001', 0, 1, 2049797085860950020, 6499.00, 0.00, 0.00, 6499.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 2, 1, 1, 'SF1234567890', '顺丰速运', DATE_SUB(NOW(), INTERVAL 20 HOUR), NULL, '', NULL, 1, '电子数码旗舰店', 0, 0),
(2, 'DG20260501002', 0, 1, 2049797085860950021, 2999.00, 0.00, 0.00, 2999.00, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 2, 1, 1, 'YT9876543210', '圆通速递', DATE_SUB(NOW(), INTERVAL 18 HOUR), NULL, '', NULL, 1, '电子数码旗舰店', 0, 0),

-- 已完成 (order_status=3)
(2, 'DG20260428001', 0, 1, 2049797085860950022, 11999.00, 0.00, 300.00, 11699.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 3, 1, 1, 'SF2345678901', '顺丰速运', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), '', NULL, 1, '电子数码旗舰店', 1, 0),
(2, 'DG20260427001', 0, 1, 2049797085860950023, 5199.00, 0.00, 0.00, 5199.00, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 3, 1, 1, 'ZT3456789012', '中通快递', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), '', NULL, 1, '电子数码旗舰店', 1, 0),
(2, 'DG20260426001', 0, 1, 2049797085860950024, 2499.00, 0.00, 0.00, 2499.00, 2, DATE_SUB(NOW(), INTERVAL 7 DAY), 3, 1, 1, 'SF4567890123', '顺丰速运', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), '满意', NULL, 1, '电子数码旗舰店', 1, 0),

-- 已取消 (order_status=4)
(2, 'DG20260425001', 0, 1, 2049786354276929537, 4399.00, 0.00, 0.00, 4399.00, 1, NULL, 4, 0, 1, NULL, NULL, NULL, NULL, '', NULL, 1, '电子数码旗舰店', 0, 0),
(2, 'DG20260424001', 0, 1, 2049790077195689985, 8999.00, 0.00, 0.00, 8999.00, 2, NULL, 4, 0, 1, NULL, NULL, NULL, NULL, '不想要了', NULL, 1, '电子数码旗舰店', 0, 0),

-- 退款中 (order_status=5)
(2, 'DG20260430001', 0, 1, 2049795525823094785, 7999.00, 0.00, 0.00, 7999.00, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 5, 3, 1, 'SF5678901234', '顺丰速运', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, '', NULL, 1, '电子数码旗舰店', 0, 0),

-- 待评价 (order_status=7, 用7表示)
(2, 'DG20260429001', 0, 1, 2049797085860950018, 15999.00, 0.00, 500.00, 15499.00, 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 3, 1, 1, 'JD6789012345', '京东物流', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), '', NULL, 1, '电子数码旗舰店', 0, 0);

-- 记录订单ID
SET @order_start = LAST_INSERT_ID();
-- 顺序: 0-1:待付款, 2-4:待发货, 5-6:待收货, 7-9:已完成, 10-11:已取消, 12:退款中, 13:待评价
SET @order_pending_pay_1 = @order_start;
SET @order_pending_pay_2 = @order_start + 1;
SET @order_pending_ship_1 = @order_start + 2;
SET @order_pending_ship_2 = @order_start + 3;
SET @order_pending_ship_3 = @order_start + 4;
SET @order_pending_recv_1 = @order_start + 5;
SET @order_pending_recv_2 = @order_start + 6;
SET @order_completed_1 = @order_start + 7;
SET @order_completed_2 = @order_start + 8;
SET @order_completed_3 = @order_start + 9;
SET @order_cancelled_1 = @order_start + 10;
SET @order_cancelled_2 = @order_start + 11;
SET @order_refunding = @order_start + 12;
SET @order_pending_review = @order_start + 13;

-- ============================================================
-- 5. 订单明细 (order_item)
-- ============================================================
INSERT INTO `order_item` (`tenant_id`, `order_id`, `spu_id`, `sku_id`, `product_name`, `spec_values`, `product_image`, `price`, `quantity`, `subtotal`, `pay_amount`, `deleted`)
VALUES
-- 待付款订单
(2, @order_pending_pay_1, @spu_huawei_mate60, @sku_mate60_256, '华为Mate 60 Pro', '{"颜色":"雅丹黑","存储":"256GB"}', '/images/product/huawei-mate60.jpg', 6999.00, 1, 6999.00, 6999.00, 0),
(2, @order_pending_pay_2, @spu_sony_xm5, @sku_sony_xm5_bk, '索尼WH-1000XM5', '{"颜色":"黑色"}', '/images/product/sony-xm5.jpg', 2499.00, 1, 2499.00, 2499.00, 0),

-- 待发货订单
(2, @order_pending_ship_1, @spu_iphone15pro, @sku_iphone_256, 'iPhone 15 Pro Max', '{"颜色":"原色钛金属","存储":"256GB"}', '/images/product/iphone15pro.jpg', 9999.00, 1, 9999.00, 9899.00, 0),
(2, @order_pending_ship_2, @spu_huawei_mate60, @sku_mate60_512_bk, '华为Mate 60 Pro', '{"颜色":"雅丹黑","存储":"512GB"}', '/images/product/huawei-mate60.jpg', 7499.00, 1, 7499.00, 6999.00, 0),
(2, @order_pending_ship_3, @spu_iphone15pro, @sku_iphone_1t, 'iPhone 15 Pro Max', '{"颜色":"白色钛金属","存储":"1TB"}', '/images/product/iphone15pro.jpg', 13999.00, 1, 13999.00, 13799.00, 0),

-- 待收货订单
(2, @order_pending_recv_1, @spu_apple_watch, @sku_watch_ti, 'Apple Watch Ultra 2', '{"尺寸":"49mm","材质":"钛金属"}', '/images/product/apple-watch-ultra2.jpg', 6499.00, 1, 6499.00, 6499.00, 0),
(2, @order_pending_recv_2, @spu_sony_xm5, @sku_sony_xm5_slv, '索尼WH-1000XM5', '{"颜色":"铂金银"}', '/images/product/sony-xm5.jpg', 2999.00, 1, 2999.00, 2999.00, 0),

-- 已完成订单
(2, @order_completed_1, @spu_thinkpad_x1, @sku_thinkpad_16, '联想ThinkPad X1 Carbon', '{"处理器":"i7-1365U","内存":"16GB","硬盘":"512GB"}', '/images/product/thinkpad-x1.jpg', 11999.00, 1, 11999.00, 11699.00, 0),
(2, @order_completed_2, @spu_matepad_pro, @sku_matepad_256, '华为MatePad Pro 13.2', '{"颜色":"曜金黑","存储":"12+256GB"}', '/images/product/matepad-pro.jpg', 5199.00, 1, 5199.00, 5199.00, 0),
(2, @order_completed_3, @spu_sony_xm5, @sku_sony_xm5_bk, '索尼WH-1000XM5', '{"颜色":"黑色"}', '/images/product/sony-xm5.jpg', 2499.00, 1, 2499.00, 2499.00, 0),

-- 已取消订单
(2, @order_cancelled_1, @spu_redmagic9, @sku_redmagic_256, '红魔9 Pro', '{"颜色":"暗夜骑士","存储":"12+256GB"}', '/images/product/redmagic9.jpg', 4399.00, 1, 4399.00, 4399.00, 0),
(2, @order_cancelled_2, @spu_ipad_pro_m4, @sku_ipad_256, 'iPad Pro M4', '{"颜色":"银色","存储":"256GB"}', '/images/product/ipad-pro-m4.jpg', 8999.00, 1, 8999.00, 8999.00, 0),

-- 退款中订单
(2, @order_refunding, @spu_huawei_mate60, @sku_mate60_512_pu, '华为Mate 60 Pro', '{"颜色":"南糯紫","存储":"512GB"}', '/images/product/huawei-mate60.jpg', 7999.00, 1, 7999.00, 7999.00, 0),

-- 待评价订单
(2, @order_pending_review, @spu_thinkpad_x1, @sku_thinkpad_32, '联想ThinkPad X1 Carbon', '{"处理器":"i7-1365U","内存":"32GB","硬盘":"1TB"}', '/images/product/thinkpad-x1.jpg', 15999.00, 1, 15999.00, 15499.00, 0);

-- 记录order_item ID
SET @item_start = LAST_INSERT_ID();
SET @item_refunding = @item_start + 12; -- 退款中的订单项

-- ============================================================
-- 6. 订单地址 (order_address)
-- ============================================================
INSERT INTO `order_address` (`order_id`, `receiver_name`, `receiver_phone`, `province_name`, `city_name`, `district_name`, `detail_address`, `full_address`, `deleted`)
VALUES
(@order_pending_pay_1, '张三', '13800138000', '北京市', '朝阳区', '望京街道', '中关村大街1号', '北京市朝阳区望京街道中关村大街1号', 0),
(@order_pending_pay_2, '李四', '13900139000', '上海市', '浦东新区', '陆家嘴', '世纪大道100号', '上海市浦东新区陆家嘴世纪大道100号', 0),
(@order_pending_ship_1, '王五', '13700137000', '广州市', '天河区', '珠江新城', '花城大道88号', '广州市天河区珠江新城花城大道88号', 0),
(@order_pending_ship_2, '赵六', '13600136000', '深圳市', '南山区', '科技园', '科技南路18号', '深圳市南山区科技园科技南路18号', 0),
(@order_pending_ship_3, '张伟', '13500135000', '杭州市', '西湖区', '文三路', '文三路478号', '杭州市西湖区文三路478号', 0),
(@order_pending_recv_1, '王芳', '13400134000', '成都市', '武侯区', '天府广场', '人民南路四段1号', '成都市武侯区天府广场人民南路四段1号', 0),
(@order_pending_recv_2, '李伟', '13300133000', '武汉市', '洪山区', '光谷', '光谷大道77号', '武汉市洪山区光谷光谷大道77号', 0),
(@order_completed_1, '赵勇', '13200132000', '南京市', '鼓楼区', '新街口', '中山路18号', '南京市鼓楼区新街口中山路18号', 0),
(@order_completed_2, '钱艳', '13100131000', '天津市', '和平区', '营口道', '营口道100号', '天津市和平区营口道100号', 0),
(@order_completed_3, '孙军', '13000130000', '重庆市', '渝中区', '解放碑', '民生路28号', '重庆市渝中区解放碑民生路28号', 0),
(@order_cancelled_1, '张三', '13800138000', '北京市', '朝阳区', '望京街道', '中关村大街1号', '北京市朝阳区望京街道中关村大街1号', 0),
(@order_cancelled_2, '李四', '13900139000', '上海市', '浦东新区', '陆家嘴', '世纪大道100号', '上海市浦东新区陆家嘴世纪大道100号', 0),
(@order_refunding, '王五', '13700137000', '广州市', '天河区', '珠江新城', '花城大道88号', '广州市天河区珠江新城花城大道88号', 0),
(@order_pending_review, '赵六', '13600136000', '深圳市', '南山区', '科技园', '科技南路18号', '深圳市南山区科技园科技南路18号', 0);

-- ============================================================
-- 7. 支付记录 (payment_record)
-- ============================================================
INSERT INTO `payment_record` (`tenant_id`, `payment_no`, `order_id`, `order_no`, `member_id`, `pay_type`, `pay_amount`, `status`, `trade_no`, `pay_time`, `deleted`)
VALUES
(2, 'PAY20260503001', @order_pending_ship_1, 'DG20260503001', 2049795525823094785, 1, 9899.00, 1, 'WX20260503001', DATE_SUB(NOW(), INTERVAL 2 HOUR), 0),
(2, 'PAY20260503002', @order_pending_ship_2, 'DG20260503002', 2049797085860950018, 2, 6999.00, 1, 'ALI20260503002', DATE_SUB(NOW(), INTERVAL 3 HOUR), 0),
(2, 'PAY20260502001', @order_pending_ship_3, 'DG20260502001', 2049797085860950019, 1, 13799.00, 1, 'WX20260502001', DATE_SUB(NOW(), INTERVAL 5 HOUR), 0),
(2, 'PAY20260501001', @order_pending_recv_1, 'DG20260501001', 2049797085860950020, 1, 6499.00, 1, 'WX20260501001', DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(2, 'PAY20260501002', @order_pending_recv_2, 'DG20260501002', 2049797085860950021, 2, 2999.00, 1, 'ALI20260501002', DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(2, 'PAY20260428001', @order_completed_1, 'DG20260428001', 2049797085860950022, 1, 11699.00, 1, 'WX20260428001', DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
(2, 'PAY20260427001', @order_completed_2, 'DG20260427001', 2049797085860950023, 1, 5199.00, 1, 'WX20260427001', DATE_SUB(NOW(), INTERVAL 6 DAY), 0),
(2, 'PAY20260426001', @order_completed_3, 'DG20260426001', 2049797085860950024, 2, 2499.00, 1, 'ALI20260426001', DATE_SUB(NOW(), INTERVAL 7 DAY), 0),
(2, 'PAY20260430001', @order_refunding, 'DG20260430001', 2049795525823094785, 1, 7999.00, 1, 'WX20260430001', DATE_SUB(NOW(), INTERVAL 3 DAY), 0),
(2, 'PAY20260429001', @order_pending_review, 'DG20260429001', 2049797085860950018, 1, 15499.00, 1, 'WX20260429001', DATE_SUB(NOW(), INTERVAL 4 DAY), 0);

-- ============================================================
-- 8. 子管理员 (补充3个，已有1个)
-- ============================================================
-- 密码均为 123456 的BCrypt hash
INSERT INTO `tenant_admin` (`tenant_id`, `username`, `password`, `real_name`, `phone`, `email`, `role_type`, `status`, `deleted`)
VALUES
(2, 'digital_ops', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李运营', '13800138002', 'ops@digital.com', 2, 1, 0),
(2, 'digital_sale', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王销售', '13800138003', 'sale@digital.com', 2, 1, 0),
(2, 'digital_cs', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵客服', '13800138004', 'cs@digital.com', 2, 0, 0);

-- ============================================================
-- 9. 结算记录 (10条，含各种状态)
-- ============================================================
-- 先删除之前测试产生的1条记录
UPDATE `tenant_settle` SET `deleted` = 1 WHERE `tenant_id` = 2;

INSERT INTO `tenant_settle` (`tenant_id`, `settle_no`, `order_amount`, `commission_amount`, `settle_amount`, `status`, `period_start`, `period_end`, `deleted`)
VALUES
-- 已结算 (status=1)
(2, 'ST20260401001', 35680.00, 1070.40, 34609.60, 1, '2026-04-01 00:00:00', '2026-04-07 23:59:59', 0),
(2, 'ST20260408001', 42350.00, 1270.50, 41079.50, 1, '2026-04-08 00:00:00', '2026-04-14 23:59:59', 0),
(2, 'ST20260415001', 28900.00, 867.00, 28033.00, 1, '2026-04-15 00:00:00', '2026-04-21 23:59:59', 0),
(2, 'ST20260422001', 51200.00, 1536.00, 49664.00, 1, '2026-04-22 00:00:00', '2026-04-28 23:59:59', 0),
(2, 'ST20260429001', 38750.00, 1162.50, 37587.50, 1, '2026-04-29 00:00:00', '2026-05-04 23:59:59', 0),

-- 待结算 (status=0)
(2, 'ST20260505001', 22480.00, 674.40, 21805.60, 0, '2026-05-05 00:00:00', '2026-05-11 23:59:59', 0),

-- 提现记录 - 用 settle_amount 存提现金额，order_amount=0 表示提现
(2, 'WD20260410001', 0.00, 0.00, 20000.00, 1, '2026-04-10 10:00:00', '2026-04-10 10:00:00', 0),
(2, 'WD20260420001', 0.00, 0.00, 15000.00, 1, '2026-04-20 14:00:00', '2026-04-20 14:00:00', 0),
(2, 'WD20260501001', 0.00, 0.00, 30000.00, 0, '2026-05-01 09:30:00', '2026-05-01 09:30:00', 0),
(2, 'WD20260503001', 0.00, 0.00, 10000.00, 0, '2026-05-03 16:00:00', '2026-05-03 16:00:00', 0);

-- ============================================================
-- 10. 退款记录 (5条，覆盖各种状态)
-- ============================================================
INSERT INTO `order_refund` (`tenant_id`, `refund_no`, `order_id`, `order_item_id`, `member_id`, `refund_type`, `refund_reason`, `refund_desc`, `refund_images`, `refund_amount`, `refund_status`, `audit_time`, `audit_remark`, `refund_time`, `deleted`)
VALUES
-- 待审核 (refund_status=0)
(2, 'RF20260504001', @order_refunding, @item_refunding, 2049795525823094785, 1, '商品质量问题', '手机屏幕出现亮点，要求退款', '', 7999.00, 0, NULL, NULL, NULL, 0),
(2, 'RF20260503001', @order_completed_3, @item_start + 9, 2049797085860950024, 2, '不想要了', '耳机佩戴不舒适，申请退货退款', '', 2499.00, 0, NULL, NULL, NULL, 0),

-- 已同意 (refund_status=1)
(2, 'RF20260502001', @order_completed_2, @item_start + 8, 2049797085860950023, 1, '与描述不符', '平板实际颜色与图片不一致', '', 5199.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), '同意退款', NULL, 0),

-- 已退款 (refund_status=3)
(2, 'RF20260429001', @order_completed_1, @item_start + 7, 2049797085860950022, 2, '商品损坏', '收到时外包装破损，电脑有划痕', '', 11699.00, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), '同意退款，已收到退货', DATE_SUB(NOW(), INTERVAL 2 DAY), 0),

-- 已拒绝 (refund_status=2)
(2, 'RF20260428001', @order_completed_3, @item_start + 9, 2049797085860950024, 1, '价格问题', '认为价格偏高要求退款', '', 2499.00, 2, DATE_SUB(NOW(), INTERVAL 4 DAY), '商品无质量问题，不支持退款', NULL, 0);

-- ============================================================
-- 11. 更新tenant表补充字段
-- ============================================================
UPDATE `tenant` SET
  `logo` = '/images/tenant/digital-logo.jpg',
  `description` = '专注于数码产品销售，正品保证，7天无理由退换',
  `score_product` = 4.8,
  `score_service` = 4.6,
  `score_logistics` = 4.7
WHERE `id` = 2;

-- ============================================================
-- 验证数据
-- ============================================================
SELECT '商品SPU' AS module, COUNT(*) AS count FROM product_spu WHERE tenant_id=2 AND deleted=0
UNION ALL
SELECT '商品SKU', COUNT(*) FROM product_sku WHERE tenant_id=2 AND deleted=0
UNION ALL
SELECT '订单', COUNT(*) FROM order_main WHERE tenant_id=2 AND deleted=0
UNION ALL
SELECT '订单明细', COUNT(*) FROM order_item WHERE tenant_id=2 AND deleted=0
UNION ALL
SELECT '子管理员', COUNT(*) FROM tenant_admin WHERE tenant_id=2 AND deleted=0
UNION ALL
SELECT '结算记录', COUNT(*) FROM tenant_settle WHERE tenant_id=2 AND deleted=0
UNION ALL
SELECT '退款记录', COUNT(*) FROM order_refund WHERE tenant_id=2 AND deleted=0;
