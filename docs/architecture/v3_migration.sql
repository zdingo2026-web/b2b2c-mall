-- ============================================================
-- B2B2C商城 V3 数据库迁移脚本
-- 新增8个Calicat设计页面相关表和字段
-- 执行前提: 已执行 init_fixed.sql + v2_migration.sql
-- ============================================================

-- ========== 1. 新增表 ==========

-- 1.1 会员银行卡
CREATE TABLE IF NOT EXISTS member_bank_card (
  id BIGINT PRIMARY KEY COMMENT '主键ID',
  member_id BIGINT NOT NULL COMMENT '用户ID',
  bank_name VARCHAR(50) NOT NULL COMMENT '银行名称',
  bank_code VARCHAR(20) COMMENT '银行编码',
  card_no VARCHAR(100) NOT NULL COMMENT '加密卡号(AES)',
  card_no_mask VARCHAR(30) NOT NULL COMMENT '掩码卡号(展示用,如 **** **** **** 8888)',
  card_type TINYINT DEFAULT 1 COMMENT '卡类型 1-借记卡 2-信用卡',
  card_color VARCHAR(20) DEFAULT '#2563EB' COMMENT '卡片展示颜色',
  bank_logo VARCHAR(500) COMMENT '银行logo URL',
  expiry_date VARCHAR(10) COMMENT '有效期(MM/YY)',
  is_default TINYINT DEFAULT 0 COMMENT '是否默认 0-否 1-是',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员银行卡';

-- ========== 2. 修改现有表 ==========

-- 2.1 member_address 增加标签和默认字段
ALTER TABLE member_address ADD COLUMN tag VARCHAR(20) DEFAULT NULL COMMENT '地址标签: 家/公司/学校' AFTER is_default;

-- 2.2 order_main 增加发票、配送、物流相关字段
ALTER TABLE order_main ADD COLUMN invoice_type TINYINT DEFAULT 0 COMMENT '发票类型 0-无 1-电子发票 2-纸质发票' AFTER is_reviewed;
ALTER TABLE order_main ADD COLUMN invoice_title VARCHAR(100) DEFAULT NULL COMMENT '发票抬头' AFTER invoice_type;
ALTER TABLE order_main ADD COLUMN delivery_method TINYINT DEFAULT 1 COMMENT '配送方式 1-快递 2-自提' AFTER invoice_title;
ALTER TABLE order_main ADD COLUMN delivery_no VARCHAR(50) DEFAULT NULL COMMENT '物流单号' AFTER delivery_method;
ALTER TABLE order_main ADD COLUMN delivery_company VARCHAR(50) DEFAULT NULL COMMENT '物流公司' AFTER delivery_no;
ALTER TABLE order_main ADD COLUMN delivery_status TINYINT DEFAULT 0 COMMENT '物流状态 0-无 1-已发货 2-运输中 3-已签收' AFTER delivery_company;

-- 注意: order_main 已有 pay_type 字段 (1-支付宝 2-微信 3-银行卡), 无需新增

-- 2.3 member_collect 增加收藏类型
ALTER TABLE member_collect ADD COLUMN collect_type TINYINT DEFAULT 1 COMMENT '收藏类型 1-商品 2-店铺 3-内容' AFTER spu_id;

-- ========== 3. 种子数据 ==========

-- 3.1 示例银行卡数据
INSERT INTO member_bank_card (id, member_id, bank_name, bank_code, card_no, card_no_mask, card_type, card_color, bank_logo, expiry_date, is_default)
SELECT 1, m.id, '中国工商银行', 'ICBC', 'ENCRYPTED_PLACEHOLDER', '**** **** **** 6688', 1, '#DC2626', '', '12/28', 1
FROM member m WHERE m.id = (SELECT MIN(id) FROM member) LIMIT 1;

INSERT INTO member_bank_card (id, member_id, bank_name, bank_code, card_no, card_no_mask, card_type, card_color, bank_logo, expiry_date, is_default)
SELECT 2, m.id, '中国建设银行', 'CCB', 'ENCRYPTED_PLACEHOLDER', '**** **** **** 3321', 2, '#2563EB', '', '06/27', 0
FROM member m WHERE m.id = (SELECT MIN(id) FROM member) LIMIT 1;

INSERT INTO member_bank_card (id, member_id, bank_name, bank_code, card_no, card_no_mask, card_type, card_color, bank_logo, expiry_date, is_default)
SELECT 3, m.id, '招商银行', 'CMB', 'ENCRYPTED_PLACEHOLDER', '**** **** **** 9907', 1, '#059669', '', '03/29', 0
FROM member m WHERE m.id = (SELECT MIN(id) FROM member) LIMIT 1;

-- 3.2 给已有地址添加标签
UPDATE member_address SET tag = '家', is_default = 1 WHERE is_default = 1 AND tag IS NULL LIMIT 5;
UPDATE member_address SET tag = '公司' WHERE is_default = 0 AND tag IS NULL LIMIT 5;

-- ========== 完成 ==========
