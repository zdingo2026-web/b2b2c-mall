-- ============================================================
-- B2B2C商城系统 - 数据库初始化脚本
-- 数据库: MySQL 5.7+
-- 字符集: utf8mb4
-- 日期: 2024-01-01
-- ============================================================

-- 彻底删除旧库（解决乱码问题）
DROP DATABASE IF EXISTS `mall`;
DROP DATABASE IF EXISTS `mall_b2b2c`;
-- 创建数据库，严格指定编码
CREATE DATABASE `mall_b2b2c` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `mall_b2b2c`;

-- ----------------------------
-- 购物车表
-- ----------------------------
DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `sku_id` bigint(20) NOT NULL COMMENT 'SKU ID',
  `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
  `spec_values` varchar(500) DEFAULT NULL COMMENT '规格属性值JSON',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) NOT NULL COMMENT '加入时价格',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `is_checked` tinyint(1) DEFAULT '1' COMMENT '是否选中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_sku` (`member_id`,`sku_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ----------------------------
-- 轮播图表
-- ----------------------------
DROP TABLE IF EXISTS `content_banner`;
CREATE TABLE `content_banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Banner ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT '商家ID',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `image_url` varchar(255) NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) DEFAULT NULL COMMENT '链接URL',
  `link_type` tinyint(4) DEFAULT '1' COMMENT '链接类型:1-商品,2-分类,3-页面,4-外链',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner轮播图表';

-- ----------------------------
-- 楼层配置表
-- ----------------------------
DROP TABLE IF EXISTS `content_floor`;
CREATE TABLE `content_floor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '楼层ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT '商家ID',
  `floor_name` varchar(100) NOT NULL COMMENT '楼层名称',
  `category_id` bigint(20) DEFAULT NULL COMMENT '关联分类ID',
  `style` tinyint(4) DEFAULT '1' COMMENT '样式:1-左右,2-上下,3-网格',
  `product_count` int(11) DEFAULT '10' COMMENT '展示商品数',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼层配置表';

-- ----------------------------
-- 运费模板表
-- ----------------------------
DROP TABLE IF EXISTS `freight_template`;
CREATE TABLE `freight_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT '商家ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `charge_type` tinyint(4) DEFAULT '1' COMMENT '计费:1-按件,2-按重量,3-按体积',
  `default_first_amount` decimal(10,2) DEFAULT '1.00' COMMENT '首件/首重/首体积',
  `default_first_price` decimal(10,2) DEFAULT '10.00' COMMENT '首费',
  `default_continue_amount` decimal(10,2) DEFAULT '1.00' COMMENT '续件/续重',
  `default_continue_price` decimal(10,2) DEFAULT '5.00' COMMENT '续费',
  `free_amount` decimal(10,2) DEFAULT NULL COMMENT '满额包邮',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='运费模板表';

-- 插入默认运费模板
LOCK TABLES `freight_template` WRITE;
INSERT INTO `freight_template` VALUES (1,0,'默认运费模板',1,1.00,10.00,1.00,5.00,99.00,1,'2024-01-01 00:00:00','2024-01-01 00:00:00',0);
UNLOCK TABLES;

-- ----------------------------
-- 会员表
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(bcrypt加密)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint(4) DEFAULT '0' COMMENT '性别:0-未知,1-男,2-女',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '余额',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除:0-正常,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2049797085860950019 DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 插入测试用户 密码都是123456
LOCK TABLES `member` WRITE;
INSERT INTO `member` VALUES
(2049786354276929537,'13800138001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','张三',NULL,'13800138001',NULL,1,1,0.00,'2024-01-01 10:00:00','2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(2049790077195689985,'13900139001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','李四',NULL,'13900139001',NULL,2,1,0.00,'2024-01-02 10:00:00','2024-01-02 00:00:00','2024-01-02 00:00:00',0),
(2049795525823094785,'13700137001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','王五',NULL,'13700137001',NULL,1,1,0.00,'2024-01-03 10:00:00','2024-01-03 00:00:00','2024-01-03 00:00:00',0),
(2049797085860950018,'13600136001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','赵六',NULL,'13600136001',NULL,0,1,0.00,'2024-01-04 10:00:00','2024-01-04 00:00:00','2024-01-04 00:00:00',0);
UNLOCK TABLES;

-- ----------------------------
-- 会员收货地址表
-- ----------------------------
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province_id` bigint(20) NOT NULL COMMENT '省ID',
  `city_id` bigint(20) NOT NULL COMMENT '市ID',
  `district_id` bigint(20) NOT NULL COMMENT '区/县ID',
  `province_name` varchar(50) DEFAULT NULL COMMENT '省名称',
  `city_name` varchar(50) DEFAULT NULL COMMENT '市名称',
  `district_name` varchar(50) DEFAULT NULL COMMENT '区/县名称',
  `detail_address` varchar(200) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认:0-否,1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_member_default` (`member_id`,`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员收货地址表';

-- ----------------------------
-- 会员第三方绑定表
-- ----------------------------
DROP TABLE IF EXISTS `member_auth`;
CREATE TABLE `member_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `identity_type` tinyint(4) NOT NULL COMMENT '认证类型:1-微信,2-QQ,3-微博',
  `identifier` varchar(100) NOT NULL COMMENT '第三方唯一标识',
  `credential` varchar(500) DEFAULT NULL COMMENT '凭证',
  `nick_name` varchar(100) DEFAULT NULL COMMENT '第三方昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '第三方头像',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_identity_type_id` (`identity_type`,`identifier`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员第三方绑定表';

-- ----------------------------
-- 会员收藏表
-- ----------------------------
DROP TABLE IF EXISTS `member_collect`;
CREATE TABLE `member_collect` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `spu_id` bigint(20) NOT NULL COMMENT '商品SPU ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员收藏表';

-- ----------------------------
-- 会员浏览足迹表
-- ----------------------------
DROP TABLE IF EXISTS `member_footprint`;
CREATE TABLE `member_footprint` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '足迹ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `spu_id` bigint(20) NOT NULL COMMENT '商品SPU ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_member_spu_date` (`member_id`,`spu_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员浏览足迹表';

-- ----------------------------
-- 订单地址快照表
-- ----------------------------
DROP TABLE IF EXISTS `order_address`;
CREATE TABLE `order_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址快照ID',
  `order_id` bigint(20) NOT NULL COMMENT '主订单ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province_name` varchar(50) NOT NULL COMMENT '省名称',
  `city_name` varchar(50) NOT NULL COMMENT '市名称',
  `district_name` varchar(50) NOT NULL COMMENT '区/县名称',
  `detail_address` varchar(200) NOT NULL COMMENT '详细地址',
  `full_address` varchar(500) DEFAULT NULL COMMENT '完整地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单地址快照表';

-- ----------------------------
-- 订单项表
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `order_id` bigint(20) NOT NULL COMMENT '主订单ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `sku_id` bigint(20) NOT NULL COMMENT 'SKU ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `spec_values` varchar(500) DEFAULT NULL COMMENT '规格属性值JSON',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) NOT NULL COMMENT '单价(快照)',
  `quantity` int(11) NOT NULL COMMENT '数量',
  `subtotal` decimal(10,2) NOT NULL COMMENT '小计',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- ----------------------------
-- 订单操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` bigint(20) NOT NULL COMMENT '主订单ID',
  `operator_type` tinyint(4) NOT NULL COMMENT '操作者:1-会员,2-商家,3-平台,4-系统',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作者ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作者名称',
  `operation_type` int(11) NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `order_status` int(11) DEFAULT NULL COMMENT '订单状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志表';

-- ----------------------------
-- 订单主表
-- ----------------------------
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '商家ID(主订单=0,子订单=商家ID)',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父订单ID',
  `order_type` tinyint(4) DEFAULT '1' COMMENT '订单类型:1-普通订单,2-秒杀订单',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `freight_amount` decimal(10,2) DEFAULT '0.00' COMMENT '运费',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `pay_type` tinyint(4) DEFAULT NULL COMMENT '支付方式:1-微信,2-支付宝,3-余额',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `order_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单状态:0-待支付,1-已支付,2-已发货,3-已完成,4-已取消,5-已退款',
  `pay_status` tinyint(4) DEFAULT '0' COMMENT '支付状态:0-未支付,1-已支付,2-已退款',
  `delivery_type` tinyint(4) DEFAULT '1' COMMENT '配送:1-快递,2-自提',
  `logistics_no` varchar(100) DEFAULT NULL COMMENT '物流单号',
  `logistics_company` varchar(50) DEFAULT NULL COMMENT '物流公司',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(200) DEFAULT NULL COMMENT '买家备注',
  `expire_time` datetime DEFAULT NULL COMMENT '订单过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  `version` int(11) DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_parent_order` (`parent_id`),
  KEY `idx_status` (`tenant_id`,`order_status`),
  KEY `idx_member_status` (`member_id`,`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_auto_cancel` (`order_status`,`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- ----------------------------
-- 退换货表
-- ----------------------------
DROP TABLE IF EXISTS `order_refund`;
CREATE TABLE `order_refund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '退换货ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `refund_no` varchar(50) NOT NULL COMMENT '退换货单号',
  `order_id` bigint(20) NOT NULL COMMENT '主订单ID',
  `order_item_id` bigint(20) NOT NULL COMMENT '订单项ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `refund_type` tinyint(4) NOT NULL COMMENT '类型:1-仅退款,2-退货退款,3-换货',
  `refund_reason` varchar(200) NOT NULL COMMENT '退款原因',
  `refund_desc` varchar(500) DEFAULT NULL COMMENT '退款说明',
  `refund_images` varchar(1000) DEFAULT NULL COMMENT '凭证图片URL列表JSON',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `refund_status` int(11) NOT NULL DEFAULT '0' COMMENT '退款状态:0-待审核,1-审核通过,2-审核拒绝,3-已退款,4-已完成',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(200) DEFAULT NULL COMMENT '审核备注',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_refund_status` (`refund_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退换货表';

-- ----------------------------
-- 支付记录表
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '商家ID',
  `payment_no` varchar(50) NOT NULL COMMENT '支付流水号',
  `order_id` bigint(20) NOT NULL COMMENT '主订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `pay_type` tinyint(4) NOT NULL COMMENT '支付方式:1-微信,2-支付宝,3-余额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态:0-待支付,1-成功,2-失败,3-已关闭',
  `trade_no` varchar(100) DEFAULT NULL COMMENT '第三方流水号',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `callback_data` text COMMENT '回调原始数据',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_trade_no` (`trade_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ----------------------------
-- 退款记录表
-- ----------------------------
DROP TABLE IF EXISTS `payment_refund`;
CREATE TABLE `payment_refund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '退款记录ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '商家ID',
  `refund_no` varchar(50) NOT NULL COMMENT '退款单号',
  `payment_id` bigint(20) NOT NULL COMMENT '支付记录ID',
  `payment_no` varchar(50) NOT NULL COMMENT '原支付流水号',
  `order_id` bigint(20) NOT NULL COMMENT '主订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `refund_reason` varchar(200) DEFAULT NULL COMMENT '退款原因',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态:0-退款中,1-成功,2-失败',
  `trade_no` varchar(100) DEFAULT NULL COMMENT '第三方退款流水号',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `callback_data` text COMMENT '退款回调数据',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_payment_id` (`payment_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- ----------------------------
-- 商品属性定义表
-- ----------------------------
DROP TABLE IF EXISTS `product_attribute`;
CREATE TABLE `product_attribute` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '属性ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT '商家ID',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `attribute_name` varchar(50) NOT NULL COMMENT '属性名称',
  `attribute_type` tinyint(4) NOT NULL COMMENT '属性类型:1-规格,2-参数',
  `input_type` tinyint(4) DEFAULT '1' COMMENT '输入方式:1-手工,2-单选,3-多选',
  `selectable_values` varchar(500) DEFAULT NULL COMMENT '可选值列表JSON',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性定义表';

-- ----------------------------
-- 商品属性值表
-- ----------------------------
DROP TABLE IF EXISTS `product_attribute_value`;
CREATE TABLE `product_attribute_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '属性值ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT '商家ID',
  `attribute_id` bigint(20) NOT NULL COMMENT '属性ID',
  `attribute_name` varchar(50) DEFAULT NULL COMMENT '属性名称',
  `attribute_type` tinyint(4) DEFAULT NULL COMMENT '属性类型',
  `spu_id` bigint(20) DEFAULT NULL COMMENT 'SPU ID',
  `attribute_value` varchar(100) NOT NULL COMMENT '属性值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_attribute_id` (`attribute_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性值表';

-- ----------------------------
-- 品牌表
-- ----------------------------
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT '商家ID(0=平台级)',
  `brand_name` varchar(100) NOT NULL COMMENT '品牌名称',
  `logo` varchar(255) DEFAULT NULL COMMENT '品牌Logo URL',
  `description` varchar(500) DEFAULT NULL COMMENT '品牌描述',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_tenant_name` (`tenant_id`,`brand_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

-- ----------------------------
-- 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT '商家ID(0=平台级)',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标URL',
  `image` varchar(255) DEFAULT NULL COMMENT '分类图片URL',
  `level` tinyint(4) NOT NULL COMMENT '层级:1-一级,2-二级,3-三级',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_parent` (`tenant_id`,`parent_id`),
  KEY `idx_tenant_level` (`tenant_id`,`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ----------------------------
-- 商品评价表
-- ----------------------------
DROP TABLE IF EXISTS `product_comment`;
CREATE TABLE `product_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `order_item_id` bigint(20) NOT NULL COMMENT '订单项ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `content` varchar(500) NOT NULL COMMENT '评价内容',
  `images` varchar(1000) DEFAULT NULL COMMENT '评价图片URL列表JSON',
  `score` tinyint(4) NOT NULL DEFAULT '5' COMMENT '评分:1-5星',
  `is_anonymous` tinyint(1) DEFAULT '0' COMMENT '是否匿名',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-隐藏,1-正常',
  `reply_content` varchar(500) DEFAULT NULL COMMENT '商家回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '商家回复时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- ----------------------------
-- 商品图片表
-- ----------------------------
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `image_url` varchar(255) NOT NULL COMMENT '图片URL',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `image_type` tinyint(4) DEFAULT '1' COMMENT '类型:1-主图,2-详情图,3-SKU图',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- ----------------------------
-- 商品SKU表
-- ----------------------------
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `sku_name` varchar(200) DEFAULT NULL COMMENT 'SKU名称',
  `sku_code` varchar(50) DEFAULT NULL COMMENT 'SKU编码',
  `image` varchar(255) DEFAULT NULL COMMENT 'SKU图片URL',
  `price` decimal(10,2) NOT NULL COMMENT '销售价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存',
  `lock_stock` int(11) DEFAULT '0' COMMENT '锁定库存',
  `sales` int(11) DEFAULT '0' COMMENT '销量',
  `spec_values` varchar(500) DEFAULT NULL COMMENT '规格属性值JSON',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '重量(kg)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_sku_code` (`tenant_id`,`sku_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- ----------------------------
-- 商品SPU表
-- ----------------------------
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `category_id` bigint(20) NOT NULL COMMENT '平台分类ID',
  `brand_id` bigint(20) DEFAULT NULL COMMENT '品牌ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `sub_title` varchar(200) DEFAULT NULL COMMENT '副标题',
  `main_image` varchar(255) DEFAULT NULL COMMENT '主图URL',
  `images` varchar(1000) DEFAULT NULL COMMENT '相册图片URL列表JSON',
  `description` text COMMENT '商品详情描述',
  `min_price` decimal(10,2) DEFAULT NULL COMMENT '最低价格',
  `max_price` decimal(10,2) DEFAULT NULL COMMENT '最高价格',
  `total_sales` int(11) DEFAULT '0' COMMENT '总销量',
  `total_stock` int(11) DEFAULT '0' COMMENT '总库存',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态:0-下架,1-上架,2-违规下架',
  `audit_status` tinyint(4) DEFAULT '0' COMMENT '审核状态:0-无需审核,1-待审核,2-审核通过,3-审核拒绝',
  `audit_remark` varchar(200) DEFAULT NULL COMMENT '审核备注',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_status` (`tenant_id`,`status`),
  KEY `idx_total_sales` (`total_sales`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU表';

-- ----------------------------
-- 地区表
-- ----------------------------
DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地区ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父级ID',
  `name` varchar(50) NOT NULL COMMENT '地区名称',
  `level` tinyint(4) NOT NULL COMMENT '层级:1-省,2-市,3-区,4-乡镇,5-村',
  `code` varchar(20) DEFAULT NULL COMMENT '行政区划代码',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区表';

-- ----------------------------
-- 系统管理员表
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '密码(bcrypt加密)',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='系统管理员表';

-- 插入超级管理员 账号admin 密码123456
LOCK TABLES `sys_admin` WRITE;
INSERT INTO `sys_admin` VALUES (1,'admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','超级管理员',NULL,NULL,1,1,'2024-01-01 10:00:00','2024-01-01 00:00:00','2024-01-01 00:00:00',0);
UNLOCK TABLES;

-- ----------------------------
-- 系统配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `config_group` varchar(50) DEFAULT NULL COMMENT '配置分组',
  `config_desc` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`config_group`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入默认配置
LOCK TABLES `sys_config` WRITE;
INSERT INTO `sys_config` VALUES
(1,'site_name','B2B2C商城','basic','站点名称',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(2,'site_logo','','basic','站点Logo',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(3,'site_description','多商家商城平台','basic','站点描述',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(4,'order_auto_cancel_minutes','30','order','订单自动取消时间(分钟)',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(5,'order_auto_confirm_days','7','order','发货后自动确认收货天数',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(6,'order_refund_days','7','order','确认收货后可申请退款天数',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(7,'default_commission_rate','5.00','settle','默认佣金比例(%)',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(8,'settle_cycle_days','7','settle','结算周期(天)',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(9,'min_withdraw_amount','100.00','settle','最低提现金额',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(10,'pay_wechat_enabled','1','pay','微信支付开关',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(11,'pay_alipay_enabled','1','pay','支付宝支付开关',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(12,'pay_balance_enabled','1','pay','余额支付开关',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(13,'sms_enabled','1','sms','短信服务开关',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(14,'upload_max_size','5','oss','上传文件最大大小(MB)',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(15,'upload_allowed_types','jpg,jpeg,png,gif,webp','oss','允许上传的文件类型',0,'2024-01-01 00:00:00','2024-01-01 00:00:00',0);
UNLOCK TABLES;

-- ----------------------------
-- 文件记录表
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT '商家ID',
  `file_name` varchar(200) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小(字节)',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `url` varchar(500) NOT NULL COMMENT '访问URL',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint(20) DEFAULT NULL COMMENT '业务ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- ----------------------------
-- 权限表
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父权限ID',
  `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
  `permission_code` varchar(100) NOT NULL COMMENT '权限编码',
  `permission_type` tinyint(4) NOT NULL COMMENT '类型:1-菜单,2-按钮,3-接口',
  `path` varchar(200) DEFAULT NULL COMMENT '菜单路由路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 插入默认角色
LOCK TABLES `sys_role` WRITE;
INSERT INTO `sys_role` VALUES
(1,'超级管理员','SUPER_ADMIN','平台超级管理员',1,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(2,'平台管理员','PLATFORM_ADMIN','平台普通管理员',1,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(3,'商家主管理员','MERCHANT_MAIN','商家主账号',1,'2024-01-01 00:00:00','2024-01-01 00:00:00',0),
(4,'商家子管理员','MERCHANT_SUB','商家子账号',1,'2024-01-01 00:00:00','2024-01-01 00:00:00',0);
UNLOCK TABLES;

-- ----------------------------
-- 角色权限关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ----------------------------
-- 商家表
-- ----------------------------
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `tenant_name` varchar(100) NOT NULL COMMENT '商家名称',
  `logo` varchar(255) DEFAULT NULL COMMENT '店铺Logo URL',
  `description` varchar(500) DEFAULT NULL COMMENT '店铺描述',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系人电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系人邮箱',
  `business_license` varchar(255) DEFAULT NULL COMMENT '营业执照URL',
  `license_image` varchar(255) DEFAULT NULL COMMENT '经营许可证URL',
  `address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态:0-待审核,1-正常,2-禁用,3-审核拒绝',
  `audit_remark` varchar(200) DEFAULT NULL COMMENT '审核备注',
  `commission_rate` decimal(5,2) DEFAULT '0.00' COMMENT '平台佣金比例(%)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- 插入平台自营商家
LOCK TABLES `tenant` WRITE;
INSERT INTO `tenant` VALUES (1,'平台自营',NULL,NULL,'管理员','13800000000',NULL,NULL,NULL,NULL,1,NULL,0.00,'2024-01-01 00:00:00','2024-01-01 00:00:00',0);
UNLOCK TABLES;

-- ----------------------------
-- 商家管理员表
-- ----------------------------
DROP TABLE IF EXISTS `tenant_admin`;
CREATE TABLE `tenant_admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '密码(bcrypt加密)',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role_type` int(11) DEFAULT NULL COMMENT '角色类型',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_username` (`tenant_id`,`username`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='商家管理员表';

-- 插入自营商家管理员 账号self 密码123456
LOCK TABLES `tenant_admin` WRITE;
INSERT INTO `tenant_admin` VALUES (1,1,'self','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','自营管理员',NULL,NULL,NULL,NULL,1,'2024-01-01 10:00:00',NULL,'2024-01-01 00:00:00','2024-01-01 00:00:00',0);
UNLOCK TABLES;

-- ----------------------------
-- 商家入驻申请表
-- ----------------------------
DROP TABLE IF EXISTS `tenant_apply`;
CREATE TABLE `tenant_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `tenant_name` varchar(100) NOT NULL COMMENT '商家名称',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系人电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系人邮箱',
  `business_license` varchar(255) NOT NULL COMMENT '营业执照URL',
  `license_image` varchar(255) DEFAULT NULL COMMENT '经营许可证URL',
  `address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `apply_status` int(11) DEFAULT '0' COMMENT '申请状态:0-待审核,1-审核通过,2-审核拒绝',
  `audit_remark` varchar(200) DEFAULT NULL COMMENT '审核备注',
  `audit_user_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '审核通过后商家ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`apply_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家入驻申请表';

-- ----------------------------
-- 商家分类表
-- ----------------------------
DROP TABLE IF EXISTS `tenant_category`;
CREATE TABLE `tenant_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标URL',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`parent_id`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家分类表';

-- ----------------------------
-- 商家结算表
-- ----------------------------
DROP TABLE IF EXISTS `tenant_settle`;
CREATE TABLE `tenant_settle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '结算ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `settle_no` varchar(50) NOT NULL COMMENT '结算单号',
  `order_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `commission_amount` decimal(10,2) NOT NULL COMMENT '佣金金额',
  `settle_amount` decimal(10,2) NOT NULL COMMENT '结算金额',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态:0-待结算,1-已结算,2-已拒绝',
  `period_start` datetime DEFAULT NULL COMMENT '结算周期开始时间',
  `period_end` datetime DEFAULT NULL COMMENT '结算周期结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settle_no` (`settle_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家结算表';
