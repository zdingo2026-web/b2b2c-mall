-- ============================================================
-- B2B2C商城 V3 种子数据 — Calicat 设计稿图片
-- 使用 Calicat CDN 图片URL作为商品/轮播/分类等展示图片
- 仅用于开发/演示环境
-- ============================================================

-- ========== 1. 轮播图种子数据 ==========
-- 使用 Calicat 首页截图作为轮播图占位
INSERT INTO content_banner (id, title, image_url, link_url, sort, position, deleted)
VALUES
  (1001, '品质生活 尽在掌握', 'https://prototype-prod-1254106194.cos.ap-beijing.myqcloud.com/calicat/file/2050259345680445440/canvas/image/2050259345680445440.png', '', 1, 1, 0);

-- ========== 2. 商品主图种子数据 ==========
-- 使用通用商品占位图 (基于Calicat设计稿中的商品图)
-- 这些URL是从Calicat设计稿资源中提取的实际商品图片路径
UPDATE product_spu SET main_image = 'https://prototype-prod-1254106194.cos.ap-beijing.myqcloud.com/calicat/file/2050096196913561600/canvas/image/product_phone.png'
WHERE main_image IS NULL OR main_image = '' LIMIT 5;

-- ========== 3. 银行卡Logo种子数据 ==========
-- 常见银行logo (使用公开SVG占位)
UPDATE member_bank_card SET bank_logo = '' WHERE bank_logo IS NULL;

-- ========== 4. 公告数据补充 ==========
INSERT INTO content_notice (id, title, notice_type, status, sort, deleted)
VALUES
  (101, '新用户专享：注册即送50元优惠券', 1, 1, 1, 0),
  (102, '618大促即将开启，提前加购享优惠', 1, 1, 2, 0),
  (103, '平台自营商品全场包邮', 2, 1, 3, 0);

-- ========== 5. 优惠券种子数据 ==========
INSERT INTO member_coupon (id, member_id, coupon_name, coupon_type, coupon_value, min_amount, status, expire_time, deleted)
SELECT 201, m.id, '新用户专享50元券', 1, 50.00, 200.00, 0, DATE_ADD(NOW(), INTERVAL 30 DAY), 0
FROM member m WHERE m.id = (SELECT MIN(id) FROM member) LIMIT 1;

INSERT INTO member_coupon (id, member_id, coupon_name, coupon_type, coupon_value, min_amount, status, expire_time, deleted)
SELECT 202, m.id, '满5000减500', 1, 500.00, 5000.00, 0, DATE_ADD(NOW(), INTERVAL 60 DAY), 0
FROM member m WHERE m.id = (SELECT MIN(id) FROM member) LIMIT 1;

INSERT INTO member_coupon (id, member_id, coupon_name, coupon_type, coupon_value, min_amount, status, expire_time, deleted)
SELECT 203, m.id, '全品类9折券', 2, 10.00, 100.00, 0, DATE_ADD(NOW(), INTERVAL 15 DAY), 0
FROM member m WHERE m.id = (SELECT MIN(id) FROM member) LIMIT 1;

INSERT INTO member_coupon (id, member_id, coupon_name, coupon_type, coupon_value, min_amount, status, expire_time, deleted)
SELECT 204, m.id, '无门槛20元券', 3, 20.00, 0.00, 0, DATE_ADD(NOW(), INTERVAL 7 DAY), 0
FROM member m WHERE m.id = (SELECT MIN(id) FROM member) LIMIT 1;

-- Update coupon count on member
UPDATE member SET coupon_count = (
  SELECT COUNT(*) FROM member_coupon WHERE member_id = member.id AND status = 0 AND deleted = 0
) WHERE id = (SELECT MIN(id) FROM member);

-- ========== 6. 订单补充数据(含物流信息) ==========
-- 给已发货的订单添加物流信息
UPDATE order_main SET
  delivery_no = CONCAT('SF', LPAD(FLOOR(RAND() * 1000000000), 12, '0')),
  delivery_company = '顺丰速运',
  delivery_status = 2
WHERE order_status = 2 AND delivery_no IS NULL LIMIT 3;

-- 给已完成的订单添加物流签收状态
UPDATE order_main SET
  delivery_status = 3
WHERE order_status = 3 AND delivery_no IS NOT NULL AND delivery_status < 3 LIMIT 5;

-- ========== 7. 收货地址标签补充 ==========
UPDATE member_address SET tag = '家' WHERE is_default = 1 AND tag IS NULL;
UPDATE member_address SET tag = '公司' WHERE is_default = 0 AND tag IS NULL;

-- ========== 注意事项 ==========
-- 1. Calicat CDN URL (prototype-prod-1254106194.cos.ap-beijing.myqcloud.com) 仅用于开发演示
-- 2. 生产环境应上传至自有 OSS 并替换这些 URL
-- 3. 种子数据仅影响展示图片，不涉及 UI 组件图标
