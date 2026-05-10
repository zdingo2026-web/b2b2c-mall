-- ============================================================
-- V7 Migration: 二期通用功能
-- 新增40张表 + 5张ALTER TABLE扩展 + 种子数据
-- 执行前提: 已执行 init_fixed.sql + v2~v6 迁移脚本
-- ============================================================

-- ============================================================
-- 第一部分：营销活动模块（12张新建表）
-- ============================================================

-- 1.1 优惠券模板表
CREATE TABLE IF NOT EXISTS coupon_template (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID，0=平台',
  coupon_name       VARCHAR(100) NOT NULL COMMENT '券名称',
  coupon_type       TINYINT(4) NOT NULL COMMENT '券类型：1-满减 2-满折 3-满赠 4-无门槛减',
  coupon_value      DECIMAL(10,2) NOT NULL COMMENT '面值/折扣率(满折如8.5表示85折)',
  min_amount        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额，0=无门槛',
  max_discount      DECIMAL(10,2) DEFAULT NULL COMMENT '满折券折扣上限金额',
  gift_sku_id       BIGINT(20) DEFAULT NULL COMMENT '满赠赠品SKU ID',
  total_count       INT(11) NOT NULL COMMENT '发放总量',
  remain_count      INT(11) NOT NULL COMMENT '剩余可领数量',
  limit_per_user    INT(11) NOT NULL DEFAULT 1 COMMENT '每人限领数量',
  issuer_type       TINYINT(4) NOT NULL COMMENT '发放主体：1-平台 2-商户',
  apply_scope       TINYINT(4) NOT NULL DEFAULT 0 COMMENT '适用范围：0-全店 1-指定分类 2-指定商品',
  apply_category_ids VARCHAR(500) DEFAULT NULL COMMENT '适用分类ID列表JSON',
  apply_spu_ids     VARCHAR(1000) DEFAULT NULL COMMENT '适用商品SPU ID列表JSON',
  valid_type        TINYINT(4) NOT NULL COMMENT '有效期类型：1-固定时间段 2-领取后N天',
  valid_start_time  DATETIME DEFAULT NULL COMMENT '固定开始时间(valid_type=1)',
  valid_end_time    DATETIME DEFAULT NULL COMMENT '固定结束时间(valid_type=1)',
  valid_days        INT(11) DEFAULT NULL COMMENT '领取后有效天数(valid_type=2)',
  can_stack_seckill TINYINT(1) NOT NULL DEFAULT 0 COMMENT '秒杀商品是否可用：0-否 1-是',
  activity_id       BIGINT(20) DEFAULT NULL COMMENT '关联活动ID(新人礼包等自动发放场景)',
  status            TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (id),
  KEY idx_tenant_status (tenant_id, status),
  KEY idx_issuer_type (issuer_type),
  KEY idx_valid_time (valid_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 1.2 秒杀活动表
CREATE TABLE IF NOT EXISTS seckill_activity (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  activity_name     VARCHAR(100) NOT NULL COMMENT '活动名称',
  start_time        DATETIME NOT NULL COMMENT '开始时间',
  end_time          DATETIME NOT NULL COMMENT '结束时间',
  payment_timeout   INT(11) NOT NULL DEFAULT 5 COMMENT '支付超时(分钟)',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_status (tenant_id, status),
  KEY idx_start_time (start_time),
  KEY idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- 1.3 秒杀商品表
CREATE TABLE IF NOT EXISTS seckill_sku (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  activity_id       BIGINT(20) NOT NULL COMMENT '秒杀活动ID',
  spu_id            BIGINT(20) NOT NULL COMMENT '商品SPU ID',
  sku_id            BIGINT(20) NOT NULL COMMENT '商品SKU ID',
  seckill_price     DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
  seckill_stock     INT(11) NOT NULL COMMENT '秒杀库存',
  seckill_sales     INT(11) NOT NULL DEFAULT 0 COMMENT '已售数量',
  limit_per_user    INT(11) NOT NULL DEFAULT 1 COMMENT '每人限购数量',
  can_use_coupon    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许使用优惠券：0-否 1-是',
  sort_order        INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_activity_sku (activity_id, sku_id),
  KEY idx_activity_id (activity_id),
  KEY idx_spu_id (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 1.4 拼团活动表
CREATE TABLE IF NOT EXISTS group_buy_activity (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  activity_name     VARCHAR(100) NOT NULL COMMENT '活动名称',
  spu_id            BIGINT(20) NOT NULL COMMENT '商品SPU ID',
  sku_id            BIGINT(20) DEFAULT NULL COMMENT '指定SKU(NULL=所有SKU参与)',
  group_price       DECIMAL(10,2) NOT NULL COMMENT '拼团价格',
  group_num         INT(11) NOT NULL COMMENT '成团人数(>=2)',
  limit_per_user    INT(11) NOT NULL DEFAULT 1 COMMENT '每人限购',
  max_groups        INT(11) NOT NULL DEFAULT 0 COMMENT '最大开团数(0=不限)',
  duration          INT(11) NOT NULL COMMENT '成团时限(小时)',
  start_time        DATETIME NOT NULL COMMENT '活动开始时间',
  end_time          DATETIME NOT NULL COMMENT '活动结束时间',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_status (tenant_id, status),
  KEY idx_spu_id (spu_id),
  KEY idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团活动表';

-- 1.5 拼团记录表
CREATE TABLE IF NOT EXISTS group_buy_record (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  activity_id       BIGINT(20) NOT NULL COMMENT '活动ID',
  head_member_id    BIGINT(20) NOT NULL COMMENT '团长会员ID',
  group_status      TINYINT(4) NOT NULL DEFAULT 0 COMMENT '拼团状态：0-拼团中 1-拼团成功 2-拼团失败',
  current_num       INT(11) NOT NULL DEFAULT 1 COMMENT '当前参团人数',
  expire_time       DATETIME NOT NULL COMMENT '成团截止时间',
  success_time      DATETIME DEFAULT NULL COMMENT '成团时间',
  fail_time         DATETIME DEFAULT NULL COMMENT '失败时间',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_activity_id (activity_id),
  KEY idx_head_member (head_member_id),
  KEY idx_group_status (group_status, expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团记录表';

-- 1.6 拼团参与记录表
CREATE TABLE IF NOT EXISTS group_buy_member (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  group_record_id   BIGINT(20) NOT NULL COMMENT '拼团记录ID',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  order_id          BIGINT(20) DEFAULT NULL COMMENT '订单ID',
  is_head           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否团长：0-否 1-是',
  join_time         DATETIME NOT NULL COMMENT '参团时间',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_record_member (group_record_id, member_id),
  KEY idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团参与记录表';

-- 1.7 团购活动表
CREATE TABLE IF NOT EXISTS groupon_activity (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  activity_name     VARCHAR(100) NOT NULL COMMENT '活动名称',
  spu_id            BIGINT(20) NOT NULL COMMENT '商品SPU ID',
  groupon_price     DECIMAL(10,2) NOT NULL COMMENT '团购价格',
  limit_per_user    INT(11) NOT NULL DEFAULT 0 COMMENT '限购数量(0=不限)',
  start_time        DATETIME NOT NULL COMMENT '活动开始时间',
  end_time          DATETIME NOT NULL COMMENT '活动结束时间',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_status (tenant_id, status),
  KEY idx_spu_id (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购活动表';

-- 1.8 团购商品表
CREATE TABLE IF NOT EXISTS groupon_product (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  activity_id       BIGINT(20) NOT NULL COMMENT '团购活动ID',
  spu_id            BIGINT(20) NOT NULL COMMENT '商品SPU ID',
  sku_id            BIGINT(20) NOT NULL COMMENT '商品SKU ID',
  groupon_price     DECIMAL(10,2) NOT NULL COMMENT '团购价',
  stock             INT(11) NOT NULL COMMENT '库存',
  sales             INT(11) NOT NULL DEFAULT 0 COMMENT '已售',
  sort_order        INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_activity_sku (activity_id, sku_id),
  KEY idx_activity_id (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购商品表';

-- 1.9 限时折扣活动表
CREATE TABLE IF NOT EXISTS discount_activity (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  activity_name     VARCHAR(100) NOT NULL COMMENT '活动名称',
  discount_type     TINYINT(4) NOT NULL COMMENT '折扣类型：1-比例折扣 2-直减',
  discount_value    DECIMAL(10,2) NOT NULL COMMENT '折扣值(比例如8.5=85折，直减如10=减10元)',
  max_discount      DECIMAL(10,2) DEFAULT NULL COMMENT '折扣上限金额(比例折扣时)',
  start_time        DATETIME NOT NULL COMMENT '活动开始时间',
  end_time          DATETIME NOT NULL COMMENT '活动结束时间',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_status (tenant_id, status),
  KEY idx_time_range (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限时折扣活动表';

-- 1.10 限时折扣商品表
CREATE TABLE IF NOT EXISTS discount_product (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  activity_id       BIGINT(20) NOT NULL COMMENT '折扣活动ID',
  spu_id            BIGINT(20) NOT NULL COMMENT '商品SPU ID',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_activity_spu (activity_id, spu_id),
  KEY idx_activity_id (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限时折扣商品表';

-- 1.11 新人礼包表
CREATE TABLE IF NOT EXISTS newcomer_pack (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  pack_name         VARCHAR(100) NOT NULL DEFAULT '新人专享礼包' COMMENT '礼包名称',
  pack_desc         VARCHAR(500) DEFAULT NULL COMMENT '礼包描述',
  enabled           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用：0-否 1-是',
  coupon_ids        VARCHAR(500) DEFAULT NULL COMMENT '包含优惠券模板ID列表JSON',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新人礼包表';

-- 1.12 首单优惠设置表
CREATE TABLE IF NOT EXISTS first_order_config (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  enabled           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用：0-否 1-是',
  discount_type     TINYINT(4) NOT NULL DEFAULT 1 COMMENT '优惠类型：1-立减 2-折扣',
  discount_value    DECIMAL(10,2) NOT NULL COMMENT '优惠值(立减金额或折扣率)',
  max_discount      DECIMAL(10,2) DEFAULT NULL COMMENT '折扣上限(折扣类型时)',
  apply_scope       TINYINT(4) NOT NULL DEFAULT 0 COMMENT '适用范围：0-全场 1-指定分类',
  apply_category_ids VARCHAR(500) DEFAULT NULL COMMENT '适用分类ID列表JSON',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首单优惠设置表';


-- ============================================================
-- 第二部分：积分商城模块（7张新建表）
-- ============================================================

-- 2.1 积分规则表
CREATE TABLE IF NOT EXISTS points_rule (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  rule_type         TINYINT(4) NOT NULL COMMENT '规则类型：1-购物 2-签到 3-评价 4-评价含图 5-注册 6-分享',
  rule_name         VARCHAR(50) NOT NULL COMMENT '规则名称',
  points_value      INT(11) NOT NULL COMMENT '基础积分值',
  multiplier        DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '积分倍率',
  daily_limit       INT(11) NOT NULL DEFAULT 0 COMMENT '每日上限(0=不限)',
  enabled           TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  sort_order        INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_rule_type (rule_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';

-- 2.2 积分消费规则表
CREATE TABLE IF NOT EXISTS points_consume_rule (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  exchange_rate     DECIMAL(10,2) NOT NULL DEFAULT 0.01 COMMENT '积分抵扣汇率(1积分=?元)',
  max_deduct_rate   DECIMAL(5,2) NOT NULL DEFAULT 30.00 COMMENT '每单最多抵扣比例(%)',
  validity_type     TINYINT(4) NOT NULL DEFAULT 1 COMMENT '有效期类型：1-永久 2-固定天数',
  validity_days     INT(11) DEFAULT NULL COMMENT '有效天数(validity_type=2)',
  expire_remind_days VARCHAR(50) DEFAULT '7,3,1' COMMENT '过期提醒天数(逗号分隔)',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分消费规则表';

-- 2.3 积分账户表
CREATE TABLE IF NOT EXISTS points_account (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  balance           INT(11) NOT NULL DEFAULT 0 COMMENT '当前积分余额',
  total_earned      INT(11) NOT NULL DEFAULT 0 COMMENT '累计获得积分',
  total_spent       INT(11) NOT NULL DEFAULT 0 COMMENT '累计消费积分',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户表';

-- 2.4 积分明细表
CREATE TABLE IF NOT EXISTS points_detail (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  biz_type          TINYINT(4) NOT NULL COMMENT '业务类型：1-购物 2-签到 3-评价 4-系统发放 5-积分兑换 6-积分过期',
  biz_id            VARCHAR(64) NOT NULL COMMENT '业务ID(订单号/兑换单号等，用于幂等)',
  change_amount     INT(11) NOT NULL COMMENT '变动积分数量(正数增加，扣减时也存正数)',
  change_type       TINYINT(4) NOT NULL COMMENT '变动类型：1-增加 2-扣减',
  balance_after     INT(11) NOT NULL DEFAULT 0 COMMENT '变动后余额',
  expire_time       DATETIME DEFAULT NULL COMMENT '过期时间(FIFO先进先出)',
  expired           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已核销/过期：0-否 1-是',
  remark            VARCHAR(200) DEFAULT NULL COMMENT '备注',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_member_id (member_id),
  KEY idx_biz_idempotent (biz_type, biz_id),
  KEY idx_expire (expired, expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分明细表';

-- 2.5 积分商品分类表
CREATE TABLE IF NOT EXISTS points_product_category (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_name     VARCHAR(50) NOT NULL COMMENT '分类名称',
  icon              VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
  sort_order        INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  status            TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品分类表';

-- 2.6 积分商品表
CREATE TABLE IF NOT EXISTS points_product (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID(0=平台)',
  category_id       BIGINT(20) DEFAULT NULL COMMENT '分类ID',
  product_name      VARCHAR(200) NOT NULL COMMENT '商品名称',
  product_image     VARCHAR(500) NOT NULL COMMENT '主图URL',
  product_images    VARCHAR(1000) DEFAULT NULL COMMENT '多图URL JSON数组',
  description       TEXT DEFAULT NULL COMMENT '商品描述(富文本)',
  exchange_type     TINYINT(4) NOT NULL DEFAULT 1 COMMENT '兑换方式：1-纯积分 2-积分+现金',
  points_price      INT(11) NOT NULL COMMENT '所需积分',
  cash_price        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '所需现金(exchange_type=2时)',
  mixed_points      INT(11) DEFAULT NULL COMMENT '混合兑换所需积分(exchange_type=2时)',
  mixed_cash        DECIMAL(10,2) DEFAULT NULL COMMENT '混合兑换所需现金(exchange_type=2时)',
  stock             INT(11) NOT NULL DEFAULT 0 COMMENT '库存',
  sales             INT(11) NOT NULL DEFAULT 0 COMMENT '已兑数量',
  status            TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
  sort_order        INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_status (tenant_id, status),
  KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品表';

-- 2.7 积分兑换订单表
CREATE TABLE IF NOT EXISTS points_order (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  order_no          VARCHAR(32) NOT NULL COMMENT '兑换订单号',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  product_id        BIGINT(20) NOT NULL COMMENT '积分商品ID',
  product_name      VARCHAR(200) NOT NULL COMMENT '商品名称快照',
  product_image     VARCHAR(500) NOT NULL COMMENT '商品主图快照',
  exchange_type     TINYINT(4) NOT NULL COMMENT '兑换方式：1-纯积分 2-积分+现金',
  points_amount     INT(11) NOT NULL COMMENT '消耗积分',
  cash_amount       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付现金',
  consignee_name    VARCHAR(50) DEFAULT NULL COMMENT '收货人',
  consignee_phone   VARCHAR(20) DEFAULT NULL COMMENT '收货电话',
  consignee_address VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待支付 1-待发货 2-已发货 3-已完成 4-已取消',
  pay_time          DATETIME DEFAULT NULL COMMENT '支付时间',
  ship_time         DATETIME DEFAULT NULL COMMENT '发货时间',
  receive_time      DATETIME DEFAULT NULL COMMENT '完成时间',
  cancel_time       DATETIME DEFAULT NULL COMMENT '取消时间',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_member_id (member_id),
  KEY idx_product_id (product_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换订单表';


-- ============================================================
-- 第三部分：店铺装修模块（4张新建表）
-- ============================================================

-- 3.1 装修页面表
CREATE TABLE IF NOT EXISTS deco_page (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  page_type         TINYINT(4) NOT NULL DEFAULT 1 COMMENT '页面类型：1-店铺首页 2-商品详情页 3-自定义页',
  page_name         VARCHAR(100) NOT NULL COMMENT '页面名称',
  component_list    TEXT NOT NULL COMMENT '组件列表JSON(有序数组，每个元素含type/props/sortOrder)',
  draft_json        TEXT DEFAULT NULL COMMENT '草稿JSON(未发布时暂存)',
  published_json    TEXT DEFAULT NULL COMMENT '已发布JSON(C端渲染此字段)',
  is_published      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发布：0-否 1-是',
  publish_time      DATETIME DEFAULT NULL COMMENT '最近发布时间',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_type (tenant_id, page_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装修页面表';

-- 3.2 装修模板表
CREATE TABLE IF NOT EXISTS deco_template (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  template_name     VARCHAR(100) NOT NULL COMMENT '模板名称',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '创建商户ID(0=平台模板)',
  source            TINYINT(4) NOT NULL DEFAULT 1 COMMENT '来源：1-平台 2-自建',
  page_type         TINYINT(4) NOT NULL DEFAULT 1 COMMENT '页面类型：1-店铺首页 2-商品详情页 3-自定义页',
  thumbnail         VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
  component_list    TEXT NOT NULL COMMENT '组件列表JSON',
  component_count   INT(11) NOT NULL DEFAULT 0 COMMENT '组件数量',
  use_count         INT(11) NOT NULL DEFAULT 0 COMMENT '使用次数',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_source (tenant_id, source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装修模板表';

-- 3.3 装修相册表
CREATE TABLE IF NOT EXISTS deco_album (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  album_name        VARCHAR(100) NOT NULL COMMENT '相册名称',
  image_count       INT(11) NOT NULL DEFAULT 0 COMMENT '图片数量',
  sort_order        INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装修相册表';

-- 3.4 装修图片表
CREATE TABLE IF NOT EXISTS deco_image (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  album_id          BIGINT(20) NOT NULL COMMENT '相册ID',
  image_url         VARCHAR(500) NOT NULL COMMENT '图片URL',
  file_size         INT(11) NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  file_type         VARCHAR(20) NOT NULL DEFAULT 'jpg' COMMENT '文件类型',
  original_name     VARCHAR(200) DEFAULT NULL COMMENT '原始文件名',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_album_id (album_id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装修图片表';


-- ============================================================
-- 第四部分：三级分销模块（6张新建表）
-- ============================================================

-- 4.1 分销配置表
CREATE TABLE IF NOT EXISTS distribution_config (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  enabled           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '分销开关：0-关闭 1-开启',
  auto_audit        TINYINT(1) NOT NULL DEFAULT 0 COMMENT '自动审核：0-否 1-是',
  commission_base   TINYINT(4) NOT NULL DEFAULT 1 COMMENT '佣金基数：1-实付金额-运费 2-商品金额',
  rate_level1       DECIMAL(5,2) NOT NULL DEFAULT 10.00 COMMENT '一级佣金比例(%)',
  rate_level2       DECIMAL(5,2) NOT NULL DEFAULT 5.00 COMMENT '二级佣金比例(%)',
  rate_level3       DECIMAL(5,2) NOT NULL DEFAULT 2.00 COMMENT '三级佣金比例(%)',
  min_withdraw      DECIMAL(10,2) NOT NULL DEFAULT 100.00 COMMENT '最低提现金额',
  freeze_days       INT(11) NOT NULL DEFAULT 7 COMMENT '佣金冻结天数',
  daily_withdraw_limit DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '每日提现限额(0=不限)',
  withdraw_methods  VARCHAR(100) NOT NULL DEFAULT 'wechat,bank' COMMENT '提现方式(逗号分隔)',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销配置表';

-- 4.2 分销商表
CREATE TABLE IF NOT EXISTS distributor (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  real_name         VARCHAR(50) NOT NULL COMMENT '真实姓名',
  phone             VARCHAR(20) NOT NULL COMMENT '手机号',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝 3-已冻结 4-已封禁',
  reject_reason     VARCHAR(200) DEFAULT NULL COMMENT '拒绝原因',
  total_commission  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计佣金',
  available_commission DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '可提现佣金',
  frozen_commission DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '冻结佣金',
  parent_id         BIGINT(20) DEFAULT NULL COMMENT '上级分销商ID(直接推荐人)',
  audit_time        DATETIME DEFAULT NULL COMMENT '审核时间',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_member (tenant_id, member_id),
  KEY idx_parent_id (parent_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商表';

-- 4.3 分销商品配置表
CREATE TABLE IF NOT EXISTS distribution_product (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  spu_id            BIGINT(20) NOT NULL COMMENT '商品SPU ID',
  use_global        TINYINT(1) NOT NULL DEFAULT 1 COMMENT '使用全局佣金比例：0-否 1-是',
  rate_level1       DECIMAL(5,2) DEFAULT NULL COMMENT '一级佣金比例(use_global=0时)',
  rate_level2       DECIMAL(5,2) DEFAULT NULL COMMENT '二级佣金比例(use_global=0时)',
  rate_level3       DECIMAL(5,2) DEFAULT NULL COMMENT '三级佣金比例(use_global=0时)',
  can_distribute    TINYINT(1) NOT NULL DEFAULT 1 COMMENT '可分销：0-否 1-是',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_spu (tenant_id, spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商品配置表';

-- 4.4 分销关系表
CREATE TABLE IF NOT EXISTS distribution_relation (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  member_id         BIGINT(20) NOT NULL COMMENT '被推荐人ID',
  parent_level1     BIGINT(20) NOT NULL COMMENT '一级推荐人(直接)',
  parent_level2     BIGINT(20) DEFAULT NULL COMMENT '二级推荐人',
  parent_level3     BIGINT(20) DEFAULT NULL COMMENT '三级推荐人',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_member (tenant_id, member_id),
  KEY idx_parent1 (parent_level1),
  KEY idx_parent2 (parent_level2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销关系表';

-- 4.5 佣金记录表
CREATE TABLE IF NOT EXISTS commission_record (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  distributor_id    BIGINT(20) NOT NULL COMMENT '分销商ID',
  order_id          BIGINT(20) NOT NULL COMMENT '订单ID',
  order_item_id     BIGINT(20) NOT NULL COMMENT '订单项ID(幂等)',
  order_no          VARCHAR(32) NOT NULL COMMENT '订单号',
  order_amount      DECIMAL(10,2) NOT NULL COMMENT '佣金基数(实付-运费)',
  commission_rate   DECIMAL(5,2) NOT NULL COMMENT '佣金比例(%)',
  commission_amount DECIMAL(10,2) NOT NULL COMMENT '佣金金额',
  commission_level  TINYINT(4) NOT NULL COMMENT '佣金层级：1-一级 2-二级 3-三级',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-冻结 1-可提现 2-提现中 3-已提现 4-已取消',
  unfreeze_time     DATETIME DEFAULT NULL COMMENT '解冻时间(创建时间+冻结天数)',
  withdraw_id       BIGINT(20) DEFAULT NULL COMMENT '关联提现记录ID',
  cancel_reason     VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_item_level (order_item_id, commission_level),
  KEY idx_distributor (distributor_id, status),
  KEY idx_unfreeze (status, unfreeze_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='佣金记录表';

-- 4.6 提现记录表
CREATE TABLE IF NOT EXISTS withdraw_record (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  distributor_id    BIGINT(20) NOT NULL COMMENT '分销商ID',
  amount            DECIMAL(10,2) NOT NULL COMMENT '提现金额',
  withdraw_method   VARCHAR(20) NOT NULL COMMENT '提现方式：wechat-微信 bank-银行卡',
  account_info      VARCHAR(500) NOT NULL COMMENT '账户信息JSON(账号/姓名/银行等)',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-审核通过 2-审核拒绝 3-打款中 4-已打款 5-打款失败',
  audit_time        DATETIME DEFAULT NULL COMMENT '审核时间',
  auditor_id        BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
  reject_reason     VARCHAR(200) DEFAULT NULL COMMENT '拒绝原因',
  payment_remark    VARCHAR(200) DEFAULT NULL COMMENT '打款备注',
  payment_time      DATETIME DEFAULT NULL COMMENT '打款时间',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_distributor (distributor_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录表';


-- ============================================================
-- 第五部分：商家完善模块（5张新建表）
-- ============================================================

-- 5.1 商家等级表
CREATE TABLE IF NOT EXISTS tenant_level (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  level_name        VARCHAR(50) NOT NULL COMMENT '等级名称',
  level_icon        VARCHAR(255) DEFAULT NULL COMMENT '等级图标URL',
  commission_discount DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '佣金折扣(0.1-1.0)',
  min_score         INT(11) NOT NULL DEFAULT 0 COMMENT '最低评分要求',
  sort_weight       INT(11) NOT NULL DEFAULT 0 COMMENT '排序权重',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家等级表';

-- 5.2 分类佣金设置表
CREATE TABLE IF NOT EXISTS category_commission (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_id       BIGINT(20) NOT NULL COMMENT '商品分类ID',
  rate_level1       DECIMAL(5,2) NOT NULL DEFAULT 10.00 COMMENT '一级佣金比例(%)',
  rate_level2       DECIMAL(5,2) NOT NULL DEFAULT 5.00 COMMENT '二级佣金比例(%)',
  rate_level3       DECIMAL(5,2) NOT NULL DEFAULT 2.00 COMMENT '三级佣金比例(%)',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类佣金设置表';

-- 5.3 商家佣金结算表
CREATE TABLE IF NOT EXISTS tenant_commission_settlement (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  settlement_no     VARCHAR(32) NOT NULL COMMENT '结算单号',
  tenant_id         BIGINT(20) NOT NULL COMMENT '商户ID',
  period_start      DATETIME NOT NULL COMMENT '结算周期开始',
  period_end        DATETIME NOT NULL COMMENT '结算周期结束',
  order_count       INT(11) NOT NULL DEFAULT 0 COMMENT '订单数',
  order_total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  platform_commission DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '平台佣金',
  merchant_amount   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商家应结金额',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待结算 1-已结算',
  settle_time       DATETIME DEFAULT NULL COMMENT '结算时间',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_settlement_no (settlement_no),
  KEY idx_tenant_id (tenant_id),
  KEY idx_period (period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家佣金结算表';

-- 5.4 商家冻结记录表
CREATE TABLE IF NOT EXISTS tenant_freeze_record (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL COMMENT '商户ID',
  action_type       TINYINT(4) NOT NULL COMMENT '操作类型：1-冻结 2-解冻 3-封禁',
  reason            VARCHAR(500) NOT NULL COMMENT '原因',
  notify_merchant   TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否通知商家',
  operator_id       BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  unfreeze_time     DATETIME DEFAULT NULL COMMENT '解冻时间(自动解冻)',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家冻结记录表';

-- 5.5 入驻设置表
CREATE TABLE IF NOT EXISTS tenant_settle_config (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  enabled           TINYINT(1) NOT NULL DEFAULT 1 COMMENT '入驻开关：0-关闭 1-开启',
  settle_notice     TEXT DEFAULT NULL COMMENT '入驻须知(富文本)',
  settle_agreement  TEXT DEFAULT NULL COMMENT '入驻协议(富文本)',
  auto_audit        TINYINT(1) NOT NULL DEFAULT 0 COMMENT '自动审核：0-否 1-是',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入驻设置表';


-- ============================================================
-- 第六部分：会员增强模块（6张新建表）
-- ============================================================

-- 6.1 红包批次表
CREATE TABLE IF NOT EXISTS red_packet_batch (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户ID(0=平台)',
  batch_name        VARCHAR(100) NOT NULL COMMENT '批次名称',
  red_packet_type   TINYINT(4) NOT NULL COMMENT '红包类型：1-满减 2-无门槛',
  face_value        DECIMAL(10,2) NOT NULL COMMENT '面值',
  min_amount        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛',
  total_count       INT(11) NOT NULL COMMENT '发放总量',
  claimed_count     INT(11) NOT NULL DEFAULT 0 COMMENT '已领取数量',
  used_count        INT(11) NOT NULL DEFAULT 0 COMMENT '已使用数量',
  valid_type        TINYINT(4) NOT NULL DEFAULT 2 COMMENT '有效期类型：1-固定时间段 2-领取后N天',
  valid_start_time  DATETIME DEFAULT NULL COMMENT '固定开始时间',
  valid_end_time    DATETIME DEFAULT NULL COMMENT '固定结束时间',
  valid_days        INT(11) DEFAULT 7 COMMENT '领取后有效天数',
  send_type         TINYINT(4) NOT NULL DEFAULT 1 COMMENT '发放方式：1-用户领取 2-系统发放',
  status            TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用 2-已结束',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包批次表';

-- 6.2 会员红包表
CREATE TABLE IF NOT EXISTS member_red_packet (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id         BIGINT(20) NOT NULL DEFAULT 0 COMMENT '发放商户ID',
  batch_id          BIGINT(20) NOT NULL COMMENT '红包批次ID',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  face_value        DECIMAL(10,2) NOT NULL COMMENT '面值',
  min_amount        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛',
  source_type       TINYINT(4) NOT NULL DEFAULT 1 COMMENT '来源：1-领取 2-系统发放 3-活动赠送',
  order_id          BIGINT(20) DEFAULT NULL COMMENT '使用时关联订单ID',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-未使用 1-已使用 2-已过期',
  valid_start_time  DATETIME NOT NULL COMMENT '生效时间',
  valid_end_time    DATETIME NOT NULL COMMENT '过期时间',
  use_time          DATETIME DEFAULT NULL COMMENT '使用时间',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_member_status (member_id, status),
  KEY idx_batch_id (batch_id),
  KEY idx_valid_end (valid_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员红包表';

-- 6.3 会员等级表
CREATE TABLE IF NOT EXISTS member_level (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  level_name        VARCHAR(50) NOT NULL COMMENT '等级名称',
  level_icon        VARCHAR(255) DEFAULT NULL COMMENT '等级图标URL',
  required_growth   INT(11) NOT NULL DEFAULT 0 COMMENT '所需成长值',
  points_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '积分倍率(>=1.0)',
  exclusive_discount DECIMAL(3,2) DEFAULT NULL COMMENT '专属折扣(0.1-1.0，NULL=无折扣)',
  sort_weight       INT(11) NOT NULL DEFAULT 0 COMMENT '排序权重',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- 6.4 签到记录表
CREATE TABLE IF NOT EXISTS member_checkin_record (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  checkin_date      DATE NOT NULL COMMENT '签到日期',
  continuous_days   INT(11) NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  points_earned     INT(11) NOT NULL DEFAULT 0 COMMENT '获得积分',
  bonus_earned      INT(11) NOT NULL DEFAULT 0 COMMENT '奖励积分(连续7天等)',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_member_date (member_id, checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 6.5 实名认证表
CREATE TABLE IF NOT EXISTS member_realname_auth (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  real_name         VARCHAR(50) NOT NULL COMMENT '真实姓名',
  id_card_type      TINYINT(4) NOT NULL DEFAULT 1 COMMENT '证件类型：1-身份证 2-护照 3-港澳通行证',
  id_card_no        VARCHAR(64) NOT NULL COMMENT '证件号(AES加密存储)',
  id_card_front     VARCHAR(500) NOT NULL COMMENT '证件正面照URL',
  id_card_back      VARCHAR(500) NOT NULL COMMENT '证件背面照URL',
  status            TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
  reject_reason     VARCHAR(200) DEFAULT NULL COMMENT '拒绝原因',
  audit_time        DATETIME DEFAULT NULL COMMENT '审核时间',
  auditor_id        BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_member_id (member_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实名认证表';

-- 6.6 支付密码重置记录表
CREATE TABLE IF NOT EXISTS member_paypwd_reset (
  id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  member_id         BIGINT(20) NOT NULL COMMENT '会员ID',
  reset_reason      VARCHAR(200) NOT NULL COMMENT '重置原因',
  operator_id       BIGINT(20) DEFAULT NULL COMMENT '操作人(平台管理员)ID',
  notify_sent       TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发短信通知',
  create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT(4) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付密码重置记录表';


-- ============================================================
-- 第七部分：ALTER TABLE 扩展（5张已有表）
-- 注意：ALTER TABLE 不支持 IF NOT EXISTS，以下语句需确认字段/索引不存在后手动执行
-- 建议使用存储过程安全添加，或逐条执行并忽略已存在的错误
-- ============================================================

-- 7.1 member_coupon 新增字段和索引
-- 说明：重构为关联模板，新增来源追踪、生效时间、商户归属、秒杀叠加标记
ALTER TABLE member_coupon
  ADD COLUMN coupon_template_id BIGINT(20) NOT NULL DEFAULT 0 COMMENT '优惠券模板ID' AFTER id,
  ADD COLUMN source_type TINYINT(4) NOT NULL DEFAULT 1 COMMENT '来源类型：1-主动领取 2-系统发放(新人礼包) 3-活动赠送' AFTER coupon_type,
  ADD COLUMN source_id BIGINT(20) DEFAULT NULL COMMENT '来源ID(活动ID/礼包ID)' AFTER source_type,
  ADD COLUMN order_id BIGINT(20) DEFAULT NULL COMMENT '使用时关联的订单ID' AFTER source_id,
  ADD COLUMN valid_start_time DATETIME NOT NULL COMMENT '生效时间' AFTER expire_time,
  ADD COLUMN tenant_id BIGINT(20) NOT NULL DEFAULT 0 COMMENT '发放商户ID' AFTER member_id,
  ADD COLUMN can_stack_seckill TINYINT(1) NOT NULL DEFAULT 0 COMMENT '秒杀商品是否可用' AFTER source_id;

-- member_coupon 新增索引
ALTER TABLE member_coupon
  ADD KEY idx_template_id (coupon_template_id),
  ADD KEY idx_member_status (member_id, status),
  ADD KEY idx_valid_end (valid_end_time);

-- 7.2 order_main 新增字段和索引
-- 说明：支持营销活动订单、双券抵扣、首单优惠、积分抵扣、红包抵扣
ALTER TABLE order_main
  ADD COLUMN order_type TINYINT(4) NOT NULL DEFAULT 1 COMMENT '订单类型：1-普通 2-秒杀 3-拼团 4-团购 5-积分兑换' AFTER tenant_id,
  ADD COLUMN activity_id BIGINT(20) DEFAULT NULL COMMENT '关联活动ID' AFTER order_type,
  ADD COLUMN group_record_id BIGINT(20) DEFAULT NULL COMMENT '拼团记录ID(拼团订单)' AFTER activity_id,
  ADD COLUMN merchant_coupon_id BIGINT(20) DEFAULT NULL COMMENT '商户优惠券ID' AFTER group_record_id,
  ADD COLUMN platform_coupon_id BIGINT(20) DEFAULT NULL COMMENT '平台优惠券ID' AFTER merchant_coupon_id,
  ADD COLUMN merchant_coupon_discount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商户优惠券优惠金额' AFTER platform_coupon_id,
  ADD COLUMN platform_coupon_discount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '平台优惠券优惠金额' AFTER merchant_coupon_discount,
  ADD COLUMN first_order_discount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '首单优惠金额' AFTER platform_coupon_discount,
  ADD COLUMN points_deduct_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额' AFTER first_order_discount,
  ADD COLUMN points_deduct_value INT(11) NOT NULL DEFAULT 0 COMMENT '积分抵扣数量' AFTER points_deduct_amount,
  ADD COLUMN red_packet_id BIGINT(20) DEFAULT NULL COMMENT '红包ID' AFTER points_deduct_value,
  ADD COLUMN red_packet_discount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '红包优惠金额' AFTER red_packet_id;

-- order_main 新增索引
ALTER TABLE order_main
  ADD KEY idx_order_type (order_type),
  ADD KEY idx_activity_id (activity_id);

-- 7.3 tenant 新增字段
-- 说明：商家等级、评分体系、冻结状态
ALTER TABLE tenant
  ADD COLUMN level_id BIGINT(20) DEFAULT NULL COMMENT '商家等级ID' AFTER brand_auth,
  ADD COLUMN score_product DECIMAL(3,1) NOT NULL DEFAULT 5.0 COMMENT '商品评分' AFTER level_id,
  ADD COLUMN score_service DECIMAL(3,1) NOT NULL DEFAULT 5.0 COMMENT '服务评分' AFTER score_product,
  ADD COLUMN score_logistics DECIMAL(3,1) NOT NULL DEFAULT 5.0 COMMENT '物流评分' AFTER score_service,
  ADD COLUMN score_composite DECIMAL(3,1) NOT NULL DEFAULT 5.0 COMMENT '综合评分' AFTER score_logistics,
  ADD COLUMN score_count INT(11) NOT NULL DEFAULT 0 COMMENT '评分人数' AFTER score_composite,
  ADD COLUMN freeze_status TINYINT(4) NOT NULL DEFAULT 0 COMMENT '冻结状态：0-正常 1-已冻结 2-已封禁' AFTER score_count;

-- 7.4 member 新增字段
-- 说明：会员等级、支付密码、实名状态
ALTER TABLE member
  ADD COLUMN level_id BIGINT(20) DEFAULT NULL COMMENT '会员等级ID' AFTER growth_value,
  ADD COLUMN has_pay_password TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否设置支付密码：0-否 1-是' AFTER level_id,
  ADD COLUMN realname_status TINYINT(4) NOT NULL DEFAULT 0 COMMENT '实名状态：0-未认证 1-待审核 2-已通过' AFTER has_pay_password;

-- 7.5 member_collect 新增字段
-- 说明：收藏降价提醒标记
ALTER TABLE member_collect
  ADD COLUMN price_decreased TINYINT(1) NOT NULL DEFAULT 0 COMMENT '收藏后是否降价：0-否 1-是' AFTER collect_type;


-- ============================================================
-- 第八部分：种子数据
-- ============================================================

-- 8.1 积分规则默认数据（6条）
INSERT INTO points_rule (rule_type, rule_name, points_value, multiplier, daily_limit, enabled, sort_order) VALUES
(1, '购物返积分', 10, 1.00, 0, 1, 1),
(2, '每日签到', 5, 1.00, 1, 1, 2),
(3, '评价商品', 10, 1.00, 5, 1, 3),
(4, '评价含图', 20, 1.00, 3, 1, 4),
(5, '注册奖励', 100, 1.00, 0, 1, 5),
(6, '分享奖励', 5, 1.00, 3, 1, 6);

-- 8.2 积分消费规则默认数据（1条）
INSERT INTO points_consume_rule (exchange_rate, max_deduct_rate, validity_type, validity_days, expire_remind_days) VALUES
(0.01, 30.00, 1, NULL, '7,3,1');

-- 8.3 分销配置默认数据（1条）
INSERT INTO distribution_config (enabled, auto_audit, commission_base, rate_level1, rate_level2, rate_level3, min_withdraw, freeze_days, daily_withdraw_limit, withdraw_methods) VALUES
(0, 0, 1, 10.00, 5.00, 2.00, 100.00, 7, 0, 'wechat,bank');

-- 8.4 新人礼包默认数据（1条，禁用状态）
INSERT INTO newcomer_pack (pack_name, pack_desc, enabled, coupon_ids) VALUES
('新人专享礼包', '新用户注册即享专属优惠券礼包', 0, NULL);

-- 8.5 首单优惠默认数据（1条，禁用状态）
INSERT INTO first_order_config (enabled, discount_type, discount_value, max_discount, apply_scope, apply_category_ids) VALUES
(0, 1, 10.00, NULL, 0, NULL);

-- 8.6 入驻设置默认数据（1条）
INSERT INTO tenant_settle_config (enabled, settle_notice, settle_agreement, auto_audit) VALUES
(1, '<p>欢迎入驻本平台，请认真阅读以下须知：</p><p>1. 提交真实有效的营业执照及法人信息</p><p>2. 确保所售商品为正品，符合国家相关法律法规</p><p>3. 遵守平台运营规范，维护消费者权益</p>', '<p>入驻协议内容（待完善）</p>', 0);


-- ============================================================
-- 迁移完成
-- 合计：40张新建表 + 5张ALTER TABLE扩展 + 6组种子数据
-- ============================================================
