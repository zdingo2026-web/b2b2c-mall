-- ============================================================
-- B2B2C商城系统 - V2迁移脚本 (基于Calicat设计稿)
-- 日期: 2026-05-01
-- 说明: 从V1 MVP升级到V2设计规范的数据结构变更
-- ============================================================

-- 1. member表 - 增加用户等级/积分/红包/编号
ALTER TABLE member ADD COLUMN member_level TINYINT DEFAULT 1 COMMENT '会员等级 1-10';
ALTER TABLE member ADD COLUMN points INT DEFAULT 0 COMMENT '积分余额';
ALTER TABLE member ADD COLUMN red_packet_balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '红包余额';
ALTER TABLE member ADD COLUMN coupon_count INT DEFAULT 0 COMMENT '优惠券数量(冗余)';
ALTER TABLE member ADD COLUMN member_no VARCHAR(20) UNIQUE COMMENT '会员编号(用于展示ID)';

-- 为现有用户生成会员编号
UPDATE member SET member_no = CONCAT('M', LPAD(id, 10, '0')) WHERE member_no IS NULL;

-- 2. order_main表 - 增加店铺名称快照和评价状态
ALTER TABLE order_main ADD COLUMN tenant_name VARCHAR(100) COMMENT '店铺名称快照';
ALTER TABLE order_main ADD COLUMN is_reviewed TINYINT DEFAULT 0 COMMENT '是否已评价 0-否 1-是';

-- 回填店铺名称
UPDATE order_main o INNER JOIN tenant t ON o.tenant_id = t.id SET o.tenant_name = t.tenant_name WHERE o.tenant_id > 0;
UPDATE order_main SET tenant_name = '平台自营' WHERE tenant_id = 0;

-- 3. product_spu表 - 增加标签和划线价
ALTER TABLE product_spu ADD COLUMN tag_type TINYINT DEFAULT 0 COMMENT '标签类型 0-无 1-百亿补贴 2-品牌特卖 3-新品首发 4-以旧换新';
ALTER TABLE product_spu ADD COLUMN original_price DECIMAL(10,2) COMMENT '划线价/原价';

-- 4. tenant表 - 增加店铺评分和品牌认证
ALTER TABLE tenant ADD COLUMN brand_verified TINYINT DEFAULT 0 COMMENT '品牌认证 0-否 1-是';
ALTER TABLE tenant ADD COLUMN score_product DECIMAL(3,1) DEFAULT 5.0 COMMENT '商品评分';
ALTER TABLE tenant ADD COLUMN score_service DECIMAL(3,1) DEFAULT 5.0 COMMENT '服务评分';
ALTER TABLE tenant ADD COLUMN score_logistics DECIMAL(3,1) DEFAULT 5.0 COMMENT '物流评分';

-- 5. product_comment表 - 增加评论点赞和标签
ALTER TABLE product_comment ADD COLUMN like_count INT DEFAULT 0 COMMENT '点赞数';
ALTER TABLE product_comment ADD COLUMN comment_tags VARCHAR(200) COMMENT '评价标签JSON数组';

-- 6. content_banner表 - 增加位置类型
ALTER TABLE content_banner ADD COLUMN position TINYINT DEFAULT 1 COMMENT '展示位置 1-首页 2-分类页';

-- ============================================================
-- 新增表
-- ============================================================

-- 7. member_coupon表 - 用户优惠券
CREATE TABLE IF NOT EXISTS member_coupon (
  id BIGINT PRIMARY KEY COMMENT '主键ID',
  member_id BIGINT NOT NULL COMMENT '用户ID',
  coupon_name VARCHAR(100) COMMENT '优惠券名称',
  coupon_type TINYINT COMMENT '1-满减 2-折扣 3-无门槛',
  coupon_value DECIMAL(10,2) COMMENT '面额/折扣率',
  min_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '使用门槛金额',
  status TINYINT DEFAULT 0 COMMENT '0-未使用 1-已使用 2-已过期',
  expire_time DATETIME COMMENT '过期时间',
  use_time DATETIME COMMENT '使用时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  INDEX idx_member_id (member_id),
  INDEX idx_status (status),
  INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

-- 8. member_message表 - 站内消息
CREATE TABLE IF NOT EXISTS member_message (
  id BIGINT PRIMARY KEY COMMENT '主键ID',
  member_id BIGINT NOT NULL COMMENT '用户ID',
  title VARCHAR(100) COMMENT '消息标题',
  content TEXT COMMENT '消息内容',
  msg_type TINYINT DEFAULT 1 COMMENT '1-系统 2-订单 3-营销',
  is_read TINYINT DEFAULT 0 COMMENT '0-未读 1-已读',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  INDEX idx_member_id (member_id),
  INDEX idx_is_read (is_read),
  INDEX idx_msg_type (msg_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- 9. content_notice表 - 首页公告
CREATE TABLE IF NOT EXISTS content_notice (
  id BIGINT PRIMARY KEY COMMENT '主键ID',
  title VARCHAR(200) NOT NULL COMMENT '公告内容',
  notice_type TINYINT DEFAULT 1 COMMENT '1-活动 2-通知 3-物流',
  status TINYINT DEFAULT 1 COMMENT '0-禁用 1-启用',
  sort INT DEFAULT 0 COMMENT '排序',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  INDEX idx_status (status),
  INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页滚动公告';

-- ============================================================
-- 初始数据
-- ============================================================

-- 插入示例公告
INSERT INTO content_notice (id, title, notice_type, status, sort) VALUES
(1, '618年中大促火热进行中，全场满200减30，上不封顶！', 1, 1, 1),
(2, '新用户注册即送50元优惠券礼包', 1, 1, 2),
(3, '平台已支持微信/支付宝双渠道支付', 2, 1, 3);

-- 插入示例消息
INSERT INTO member_message (id, member_id, title, content, msg_type, is_read) VALUES
(1, 1, '欢迎加入B2B2C商城', '感谢您注册成为我们的会员，开始您的购物之旅吧！', 1, 0);
