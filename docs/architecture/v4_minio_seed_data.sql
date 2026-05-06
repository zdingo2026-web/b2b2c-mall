-- ============================================================
-- B2B2C商城 V4 种子数据 — MinIO 图片URL
-- 将所有空图片字段更新为 MinIO 上的演示图片
-- 前提：已运行 scripts/init-minio-demo.sh 上传图片到 MinIO
-- 仅用于开发/演示环境
-- ============================================================
USE `mall_b2b2c`;

-- MinIO URL 前缀（本地开发环境）
SET @minio_prefix = 'http://localhost:9000/mall-dev';

-- ========== 1. 轮播图图片 ==========
UPDATE content_banner SET image_url = CONCAT(@minio_prefix, '/banner/banner-home.jpg') WHERE id = 1 OR (id IS NOT NULL AND sort = 1) LIMIT 1;
UPDATE content_banner SET image_url = CONCAT(@minio_prefix, '/banner/banner-phone.jpg') WHERE image_url = '' AND sort = 2 LIMIT 1;
UPDATE content_banner SET image_url = CONCAT(@minio_prefix, '/banner/banner-appliance.jpg') WHERE image_url = '' AND sort = 3 LIMIT 1;
UPDATE content_banner SET image_url = CONCAT(@minio_prefix, '/banner/banner-fashion.jpg') WHERE image_url = '' AND sort = 4 LIMIT 1;
UPDATE content_banner SET image_url = CONCAT(@minio_prefix, '/banner/banner-flash-sale.jpg') WHERE image_url = '' AND sort = 5 LIMIT 1;

-- 如果 content_banner 表为空，插入轮播图
INSERT INTO content_banner (title, image_url, link_url, link_type, sort, status, tenant_id, deleted)
SELECT '品质生活 尽在掌握', CONCAT(@minio_prefix, '/banner/banner-home.jpg'), '', 1, 1, 1, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM content_banner LIMIT 1);

INSERT INTO content_banner (title, image_url, link_url, link_type, sort, status, tenant_id, deleted)
SELECT '手机专场 新品首发', CONCAT(@minio_prefix, '/banner/banner-phone.jpg'), '/product/1', 1, 2, 1, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM content_banner WHERE sort = 2 LIMIT 1);

INSERT INTO content_banner (title, image_url, link_url, link_type, sort, status, tenant_id, deleted)
SELECT '家电焕新 满1000减100', CONCAT(@minio_prefix, '/banner/banner-appliance.jpg'), '/category/3', 1, 3, 1, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM content_banner WHERE sort = 3 LIMIT 1);

INSERT INTO content_banner (title, image_url, link_url, link_type, sort, status, tenant_id, deleted)
SELECT '时尚穿搭 春夏新品', CONCAT(@minio_prefix, '/banner/banner-fashion.jpg'), '/category/4', 1, 4, 1, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM content_banner WHERE sort = 4 LIMIT 1);

INSERT INTO content_banner (title, image_url, link_url, link_type, sort, status, tenant_id, deleted)
SELECT '限时秒杀 低至5折', CONCAT(@minio_prefix, '/banner/banner-flash-sale.jpg'), '/activity/flash', 1, 5, 1, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM content_banner WHERE sort = 5 LIMIT 1);

-- 通用兜底：所有仍为空图片的轮播图
UPDATE content_banner SET image_url = CONCAT(@minio_prefix, '/banner/banner-home.jpg') WHERE image_url = '' OR image_url IS NULL;

-- ========== 2. 商品主图 (product_spu) ==========
-- 手机分类 (category_id=11)
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/phone-1.jpg') WHERE main_image = '' AND category_id = 11 AND brand_id = 1 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/phone-2.jpg') WHERE main_image = '' AND category_id = 11 AND brand_id = 2 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/phone-3.jpg') WHERE main_image = '' AND category_id = 11 AND brand_id = 3 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/phone-4.jpg') WHERE main_image = '' AND category_id = 11 AND brand_id = 4 LIMIT 1;

-- 电脑分类 (category_id=16)
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/laptop-1.jpg') WHERE main_image = '' AND category_id = 16 AND brand_id = 5 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/laptop-2.jpg') WHERE main_image = '' AND category_id = 16 AND brand_id = 6 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/laptop-3.jpg') WHERE main_image = '' AND category_id = 16 AND brand_id = 3 LIMIT 1;

-- 家电分类 (category_id=22,23,24)
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/appliance-ac.jpg') WHERE main_image = '' AND category_id = 24 AND brand_id = 9 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/appliance-fridge.jpg') WHERE main_image = '' AND category_id = 22 AND brand_id = 8 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/appliance-washer.jpg') WHERE main_image = '' AND category_id = 23 AND brand_id = 7 LIMIT 1;

-- 食品分类 (category_id=32)
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/food-milk-1.jpg') WHERE main_image = '' AND category_id = 32 AND brand_id = 14 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/food-milk-2.jpg') WHERE main_image = '' AND category_id = 32 AND brand_id = 13 LIMIT 1;
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/food-water.jpg') WHERE main_image = '' AND category_id = 35 AND brand_id = 15 LIMIT 1;

-- 通用兜底：所有仍为空主图的商品
UPDATE product_spu SET main_image = CONCAT(@minio_prefix, '/product/phone-1.jpg') WHERE main_image = '' OR main_image IS NULL;

-- ========== 3. 商品多图 (product_spu.images) ==========
UPDATE product_spu SET images = main_image WHERE (images IS NULL OR images = '') AND main_image != '';

-- ========== 4. 商品SKU图片 ==========
UPDATE product_sku SET image = (
    SELECT main_image FROM product_spu WHERE product_spu.id = product_sku.spu_id
) WHERE (image IS NULL OR image = '') AND spu_id IN (SELECT id FROM product_spu WHERE main_image != '');

-- ========== 5. 分类图标 (product_category) ==========
-- 一级分类 (level=1)
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/mobile-digital.jpg') WHERE category_name = '手机数码' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/computer-office.jpg') WHERE category_name = '电脑办公' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/home-appliance.jpg') WHERE category_name = '家用电器' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/clothing.jpg') WHERE category_name = '服装服饰' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/shoes-bags.jpg') WHERE category_name = '鞋靴箱包' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/sports-outdoor.jpg') WHERE category_name = '运动户外' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/mom-baby.jpg') WHERE category_name = '母婴用品' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/food-drink.jpg') WHERE category_name = '食品饮料' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/home-furnishing.jpg') WHERE category_name = '家居家装' AND level = 1;
UPDATE product_category SET icon = CONCAT(@minio_prefix, '/category/beauty.jpg') WHERE category_name = '美妆护肤' AND level = 1;

-- 分类大图
UPDATE product_category SET image = icon WHERE (image IS NULL OR image = '') AND icon != '' AND level = 1;

-- 二级/三级分类：继承父分类图标
UPDATE product_category c2
JOIN product_category c1 ON c2.parent_id = c1.id AND c1.level = 1
SET c2.icon = c1.icon
WHERE (c2.icon = '' OR c2.icon IS NULL) AND c1.icon != '';

UPDATE product_category c3
JOIN product_category c2 ON c3.parent_id = c2.id AND c2.level = 2
SET c3.icon = c2.icon
WHERE (c3.icon = '' OR c3.icon IS NULL) AND c2.icon != '';

-- 分类大图（所有级别）
UPDATE product_category SET image = icon WHERE (image IS NULL OR image = '') AND icon != '';

-- ========== 6. 品牌Logo (product_brand) ==========
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/apple.jpg') WHERE brand_name = '苹果';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/huawei.jpg') WHERE brand_name = '华为';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/xiaomi.jpg') WHERE brand_name = '小米';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/samsung.jpg') WHERE brand_name = '三星';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/lenovo.jpg') WHERE brand_name = '联想';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/dell.jpg') WHERE brand_name = '戴尔';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/haier.jpg') WHERE brand_name = '海尔';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/midea.jpg') WHERE brand_name = '美的';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/gree.jpg') WHERE brand_name = '格力';
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/nike.jpg') WHERE brand_name = '耐克';

-- 通用兜底：所有仍为空logo的品牌
UPDATE product_brand SET logo = CONCAT(@minio_prefix, '/brand/apple.jpg') WHERE logo = '' OR logo IS NULL;

-- ========== 7. 平台自营商家Logo ==========
UPDATE tenant SET logo = CONCAT(@minio_prefix, '/tenant/self-logo.jpg') WHERE id = 1 AND (logo IS NULL OR logo = '');

-- ========== 8. 商家管理员头像 ==========
UPDATE tenant_admin SET avatar = CONCAT(@minio_prefix, '/tenant/self-logo.jpg') WHERE tenant_id = 1 AND (avatar IS NULL OR avatar = '');

-- ========== 注意事项 ==========
-- 1. 本脚本依赖 MinIO 服务已启动且图片已上传（运行 scripts/init-minio-demo.sh）
-- 2. MinIO bucket (mall-dev) 需设置为公开读策略
-- 3. 图片URL基于 localhost:9000，Docker 环境需替换为实际 MinIO 地址
-- 4. 生产环境应替换为正式 OSS URL
