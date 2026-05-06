-- ============================================================
-- B2B2C商城系统 - 第一期数据库初始化脚本
-- 数据库: MySQL 5.7
-- 字符集: utf8mb4
-- 日期: 2026-04-30 (updated to match entity schema)
-- ============================================================

CREATE DATABASE IF NOT EXISTS `mall_b2b2c` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `mall_b2b2c`;

DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'è´­ç‰©è½¦é¡¹ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `sku_id` bigint(20) NOT NULL COMMENT 'SKU ID',
  `product_name` varchar(200) DEFAULT NULL,
  `spec_values` varchar(500) DEFAULT NULL,
  `product_image` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL COMMENT 'åŠ å…¥æ—¶ä»·æ ¼',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT 'æ•°é‡',
  `is_checked` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦é€‰ä¸­',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_sku` (`member_id`,`sku_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='è´­ç‰©è½¦è¡¨';
LOCK TABLES `cart_item` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `content_banner`;
CREATE TABLE `content_banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Banner ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT 'å•†æˆ·ID',
  `title` varchar(100) DEFAULT NULL COMMENT 'æ ‡é¢˜',
  `image_url` varchar(255) NOT NULL COMMENT 'å›¾ç‰‡URL',
  `link_url` varchar(500) DEFAULT NULL COMMENT 'é“¾æŽ¥URL',
  `link_type` tinyint(4) DEFAULT '1' COMMENT 'é“¾æŽ¥ç±»åž‹:1-å•†å“,2-åˆ†ç±»,3-é¡µé¢,4-å¤–é“¾',
  `sort` int(11) DEFAULT '0' COMMENT 'æŽ’åº',
  `start_time` datetime DEFAULT NULL COMMENT 'å¼€å§‹æ—¶é—´',
  `end_time` datetime DEFAULT NULL COMMENT 'ç»“æŸæ—¶é—´',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€:0-ç¦ç”¨,1-æ­£å¸¸',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bannerè¡¨';
LOCK TABLES `content_banner` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `content_floor`;
CREATE TABLE `content_floor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'æ¥¼å±‚ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT 'å•†æˆ·ID',
  `floor_name` varchar(100) NOT NULL COMMENT 'æ¥¼å±‚åç§°',
  `category_id` bigint(20) DEFAULT NULL COMMENT 'å…³è”åˆ†ç±»ID',
  `style` tinyint(4) DEFAULT '1' COMMENT 'æ ·å¼:1-å·¦å³,2-ä¸Šä¸‹,3-ç½‘æ ¼',
  `product_count` int(11) DEFAULT '10' COMMENT 'å±•ç¤ºå•†å“æ•°',
  `sort` int(11) DEFAULT '0' COMMENT 'æŽ’åº',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='æ¥¼å±‚é…ç½®è¡¨';
LOCK TABLES `content_floor` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `freight_template`;
CREATE TABLE `freight_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'æ¨¡æ¿ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT 'å•†æˆ·ID',
  `template_name` varchar(100) NOT NULL COMMENT 'æ¨¡æ¿åç§°',
  `charge_type` tinyint(4) DEFAULT '1' COMMENT 'è®¡è´¹:1-æŒ‰ä»¶,2-æŒ‰é‡é‡,3-æŒ‰ä½“ç§¯',
  `default_first_amount` decimal(10,2) DEFAULT '1.00' COMMENT 'é¦–ä»¶/é¦–é‡/é¦–ä½“ç§¯',
  `default_first_price` decimal(10,2) DEFAULT '10.00' COMMENT 'é¦–è´¹',
  `default_continue_amount` decimal(10,2) DEFAULT '1.00' COMMENT 'ç»­ä»¶/ç»­é‡',
  `default_continue_price` decimal(10,2) DEFAULT '5.00' COMMENT 'ç»­è´¹',
  `free_amount` decimal(10,2) DEFAULT NULL COMMENT 'æ»¡é¢åŒ…é‚®',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='è¿è´¹æ¨¡æ¿è¡¨';
LOCK TABLES `freight_template` WRITE;
INSERT INTO `freight_template` VALUES (1,0,'é»˜è®¤è¿è´¹æ¨¡æ¿',1,1.00,10.00,1.00,5.00,99.00,1,'2026-04-30 09:39:09','2026-04-30 09:39:09',0);
UNLOCK TABLES;
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ä¼šå‘˜ID',
  `username` varchar(50) DEFAULT NULL COMMENT 'ç”¨æˆ·å',
  `password` varchar(100) NOT NULL COMMENT 'å¯†ç (bcryptåŠ å¯†)',
  `nickname` varchar(50) DEFAULT NULL COMMENT 'æ˜µç§°',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'å¤´åƒURL',
  `phone` varchar(20) DEFAULT NULL COMMENT 'æ‰‹æœºå·',
  `email` varchar(100) DEFAULT NULL COMMENT 'é‚®ç®±',
  `gender` tinyint(4) DEFAULT '0' COMMENT 'æ€§åˆ«:0-æœªçŸ¥,1-ç”·,2-å¥³',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€:0-ç¦ç”¨,1-æ­£å¸¸',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT 'ä½™é¢',
  `last_login_time` datetime DEFAULT NULL COMMENT 'æœ€åŽç™»å½•æ—¶é—´',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤:0-æ­£å¸¸,1-å·²åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2049797085860950019 DEFAULT CHARSET=utf8mb4 COMMENT='ä¼šå‘˜è¡¨';
LOCK TABLES `member` WRITE;
INSERT INTO `member` VALUES (2049786354276929537,'13800138001','$2a$12$dYh3VrbxtJoQBYytZBSQ1.1JpeQWmwNU0wucU1v7xLZb5TwjKPwxu','用户8001',NULL,'13800138001',NULL,0,1,0.00,'2026-04-30 17:52:40','2026-04-30 17:42:05','2026-04-30 17:42:05',0),(2049790077195689985,'13900139001','$2a$12$wp8LV.LpoVHBzOS6i8xA0uapHaMY/N7pYJijvjUQkRQiXhdVJqNk6','用户9001',NULL,'13900139001',NULL,0,1,0.00,'2026-04-30 17:57:17','2026-04-30 17:56:52','2026-04-30 17:56:52',0),(2049795525823094785,'13700137001','$2a$12$9Q6ZEFnb.vEcwmBxx0tOmejrs5Q6mcD9/Y19fn6AoDqg6yM9kfnJ2','用户7001',NULL,'13700137001',NULL,0,1,0.00,'2026-04-30 18:18:32','2026-04-30 18:18:31','2026-04-30 18:18:31',0),(2049797085860950018,'13600136001','$2a$12$hIBUf0Kzw5LSdgYAxcbpEuX/fJQaTSCaDvF5oT2dEgZE0O27h7sv.','用户6001',NULL,'13600136001',NULL,0,1,0.00,'2026-04-30 22:37:07','2026-04-30 18:24:43','2026-04-30 18:24:43',0);
UNLOCK TABLES;
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'åœ°å€ID',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `receiver_name` varchar(50) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `province_id` bigint(20) NOT NULL COMMENT 'çœID',
  `city_id` bigint(20) NOT NULL COMMENT 'å¸‚ID',
  `district_id` bigint(20) NOT NULL COMMENT 'åŒº/åŽ¿ID',
  `province_name` varchar(50) DEFAULT NULL,
  `city_name` varchar(50) DEFAULT NULL,
  `district_name` varchar(50) DEFAULT NULL,
  `detail_address` varchar(200) NOT NULL,
  `is_default` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦é»˜è®¤:0-å¦,1-æ˜¯',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_member_default` (`member_id`,`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ä¼šå‘˜æ”¶è´§åœ°å€è¡¨';
LOCK TABLES `member_address` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `member_auth`;
CREATE TABLE `member_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ç»‘å®šID',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `identity_type` tinyint(4) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `credential` varchar(500) DEFAULT NULL,
  `nick_name` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_identity_type_id` (`identity_type`,`identifier`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ä¼šå‘˜ç¬¬ä¸‰æ–¹ç»‘å®šè¡¨';
LOCK TABLES `member_auth` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `member_collect`;
CREATE TABLE `member_collect` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'æ”¶è—ID',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `spu_id` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ä¼šå‘˜æ”¶è—è¡¨';
LOCK TABLES `member_collect` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `member_footprint`;
CREATE TABLE `member_footprint` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'è¶³è¿¹ID',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'å•†å“SPU ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'æµè§ˆæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_member_spu_date` (`member_id`,`spu_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ä¼šå‘˜æµè§ˆè¶³è¿¹è¡¨';
LOCK TABLES `member_footprint` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `order_address`;
CREATE TABLE `order_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'åœ°å€å¿«ç…§ID',
  `order_id` bigint(20) NOT NULL COMMENT 'ä¸»è®¢å•ID',
  `receiver_name` varchar(50) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `province_name` varchar(50) NOT NULL,
  `city_name` varchar(50) NOT NULL,
  `district_name` varchar(50) NOT NULL,
  `detail_address` varchar(200) NOT NULL,
  `full_address` varchar(500) DEFAULT NULL COMMENT 'å®Œæ•´åœ°å€',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='è®¢å•åœ°å€å¿«ç…§è¡¨';
LOCK TABLES `order_address` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'æ˜Žç»†ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `order_id` bigint(20) NOT NULL COMMENT 'ä¸»è®¢å•ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `sku_id` bigint(20) NOT NULL COMMENT 'SKU ID',
  `product_name` varchar(200) NOT NULL,
  `spec_values` varchar(500) DEFAULT NULL,
  `product_image` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL COMMENT 'å•ä»·(å¿«ç…§)',
  `quantity` int(11) NOT NULL COMMENT 'æ•°é‡',
  `subtotal` decimal(10,2) NOT NULL,
  `pay_amount` decimal(10,2) NOT NULL COMMENT 'å®žä»˜',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='è®¢å•æ˜Žç»†è¡¨';
LOCK TABLES `order_item` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'æ—¥å¿—ID',
  `order_id` bigint(20) NOT NULL COMMENT 'ä¸»è®¢å•ID',
  `operator_type` tinyint(4) NOT NULL COMMENT 'æ“ä½œè€…:1-ä¼šå‘˜,2-å•†æˆ·,3-å¹³å°,4-ç³»ç»Ÿ',
  `operator_id` bigint(20) DEFAULT NULL COMMENT 'æ“ä½œè€…ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT 'æ“ä½œè€…åç§°',
  `operation_type` int(11) NOT NULL,
  `operation_desc` varchar(500) DEFAULT NULL,
  `order_status` int(11) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='è®¢å•æ“ä½œæ—¥å¿—è¡¨';
LOCK TABLES `order_log` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'è®¢å•ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'å•†æˆ·ID(ä¸»è®¢å•=0,å­è®¢å•=å•†æˆ·ID)',
  `order_no` varchar(50) NOT NULL COMMENT 'è®¢å•å·',
  `parent_id` bigint(20) DEFAULT '0',
  `order_type` tinyint(4) DEFAULT '1' COMMENT 'è®¢å•ç±»åž‹',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT 'è®¢å•æ€»é‡‘é¢',
  `freight_amount` decimal(10,2) DEFAULT '0.00' COMMENT 'è¿è´¹',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT 'ä¼˜æƒ é‡‘é¢',
  `pay_amount` decimal(10,2) NOT NULL COMMENT 'å®žä»˜é‡‘é¢',
  `pay_type` tinyint(4) DEFAULT NULL COMMENT 'æ”¯ä»˜æ–¹å¼:1-å¾®ä¿¡,2-æ”¯ä»˜å®,3-ä½™é¢',
  `pay_time` datetime DEFAULT NULL COMMENT 'æ”¯ä»˜æ—¶é—´',
  `order_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'è®¢å•çŠ¶æ€',
  `pay_status` tinyint(4) DEFAULT '0' COMMENT 'æ”¯ä»˜çŠ¶æ€',
  `delivery_type` tinyint(4) DEFAULT '1' COMMENT 'é…é€:1-å¿«é€’,2-è‡ªæ',
  `logistics_no` varchar(100) DEFAULT NULL,
  `logistics_company` varchar(50) DEFAULT NULL,
  `delivery_time` datetime DEFAULT NULL COMMENT 'å‘è´§æ—¶é—´',
  `receive_time` datetime DEFAULT NULL COMMENT 'æ”¶è´§æ—¶é—´',
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT 'å–æ¶ˆåŽŸå› ',
  `remark` varchar(200) DEFAULT NULL COMMENT 'ä¹°å®¶å¤‡æ³¨',
  `expire_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  `version` int(11) DEFAULT '1' COMMENT 'ä¹è§‚é”ç‰ˆæœ¬å·',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_parent_order` (`parent_id`),
  KEY `idx_status` (`tenant_id`,`order_status`),
  KEY `idx_member_status` (`member_id`,`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_auto_cancel` (`order_status`,`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='è®¢å•ä¸»è¡¨';
LOCK TABLES `order_main` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `order_refund`;
CREATE TABLE `order_refund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'é€€æ¢è´§ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `refund_no` varchar(50) NOT NULL COMMENT 'é€€æ¢è´§å•å·',
  `order_id` bigint(20) NOT NULL COMMENT 'ä¸»è®¢å•ID',
  `order_item_id` bigint(20) NOT NULL COMMENT 'è®¢å•æ˜Žç»†ID',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `refund_type` tinyint(4) NOT NULL COMMENT 'ç±»åž‹:1-ä»…é€€æ¬¾,2-é€€è´§é€€æ¬¾,3-æ¢è´§',
  `refund_reason` varchar(200) NOT NULL COMMENT 'åŽŸå› ',
  `refund_desc` varchar(500) DEFAULT NULL,
  `refund_images` varchar(1000) DEFAULT NULL COMMENT 'å‡­è¯å›¾ç‰‡',
  `refund_amount` decimal(10,2) NOT NULL COMMENT 'é€€æ¬¾é‡‘é¢',
  `refund_status` int(11) NOT NULL DEFAULT '0',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `audit_remark` varchar(200) DEFAULT NULL,
  `refund_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_refund_status` (`refund_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='é€€æ¢è´§è¡¨';
LOCK TABLES `order_refund` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'æ”¯ä»˜è®°å½•ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT 'å•†æˆ·ID',
  `payment_no` varchar(50) NOT NULL COMMENT 'æ”¯ä»˜æµæ°´å·',
  `order_id` bigint(20) NOT NULL COMMENT 'ä¸»è®¢å•ID',
  `order_no` varchar(50) NOT NULL COMMENT 'è®¢å•å·',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `pay_type` tinyint(4) NOT NULL COMMENT 'æ”¯ä»˜æ–¹å¼:1-å¾®ä¿¡,2-æ”¯ä»˜å®,3-ä½™é¢',
  `pay_amount` decimal(10,2) NOT NULL COMMENT 'æ”¯ä»˜é‡‘é¢',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'çŠ¶æ€:0-å¾…æ”¯ä»˜,1-æˆåŠŸ,2-å¤±è´¥,3-å·²å…³é—­',
  `trade_no` varchar(100) DEFAULT NULL COMMENT 'ç¬¬ä¸‰æ–¹æµæ°´å·',
  `pay_time` datetime DEFAULT NULL COMMENT 'æ”¯ä»˜æ—¶é—´',
  `callback_data` text COMMENT 'å›žè°ƒåŽŸå§‹æ•°æ®',
  `expire_time` datetime DEFAULT NULL COMMENT 'è¿‡æœŸæ—¶é—´',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_trade_no` (`trade_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='æ”¯ä»˜è®°å½•è¡¨';
LOCK TABLES `payment_record` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `payment_refund`;
CREATE TABLE `payment_refund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'é€€æ¬¾è®°å½•ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT 'å•†æˆ·ID',
  `refund_no` varchar(50) NOT NULL COMMENT 'é€€æ¬¾å•å·',
  `payment_id` bigint(20) NOT NULL COMMENT 'æ”¯ä»˜è®°å½•ID',
  `payment_no` varchar(50) NOT NULL COMMENT 'åŽŸæ”¯ä»˜æµæ°´å·',
  `order_id` bigint(20) NOT NULL COMMENT 'ä¸»è®¢å•ID',
  `order_no` varchar(50) NOT NULL COMMENT 'è®¢å•å·',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `refund_amount` decimal(10,2) NOT NULL COMMENT 'é€€æ¬¾é‡‘é¢',
  `refund_reason` varchar(200) DEFAULT NULL COMMENT 'é€€æ¬¾åŽŸå› ',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'çŠ¶æ€:0-é€€æ¬¾ä¸­,1-æˆåŠŸ,2-å¤±è´¥',
  `trade_no` varchar(100) DEFAULT NULL COMMENT 'ç¬¬ä¸‰æ–¹é€€æ¬¾æµæ°´å·',
  `refund_time` datetime DEFAULT NULL COMMENT 'é€€æ¬¾æ—¶é—´',
  `callback_data` text COMMENT 'é€€æ¬¾å›žè°ƒæ•°æ®',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_payment_id` (`payment_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='é€€æ¬¾è®°å½•è¡¨';
LOCK TABLES `payment_refund` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product_attribute`;
CREATE TABLE `product_attribute` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'å±žæ€§ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT 'å•†æˆ·ID',
  `category_id` bigint(20) DEFAULT NULL,
  `attribute_name` varchar(50) NOT NULL,
  `attribute_type` tinyint(4) NOT NULL,
  `input_type` tinyint(4) DEFAULT '1' COMMENT 'è¾“å…¥:1-æ‰‹å·¥,2-å•é€‰,3-å¤šé€‰',
  `selectable_values` varchar(500) DEFAULT NULL,
  `sort_order` int(11) DEFAULT '0',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†å“å±žæ€§å®šä¹‰è¡¨';
LOCK TABLES `product_attribute` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product_attribute_value`;
CREATE TABLE `product_attribute_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'å±žæ€§å€¼ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT 'å•†æˆ·ID',
  `attribute_id` bigint(20) NOT NULL COMMENT 'å±žæ€§ID',
  `attribute_name` varchar(50) DEFAULT NULL,
  `attribute_type` tinyint(4) DEFAULT NULL,
  `spu_id` bigint(20) DEFAULT NULL COMMENT 'SPU ID',
  `attribute_value` varchar(100) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_attribute_id` (`attribute_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†å“å±žæ€§å€¼è¡¨';
LOCK TABLES `product_attribute_value` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'å“ç‰ŒID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT 'å•†æˆ·ID(0=å¹³å°çº§)',
  `brand_name` varchar(100) NOT NULL,
  `logo` varchar(255) DEFAULT NULL COMMENT 'å“ç‰ŒLogo',
  `description` varchar(500) DEFAULT NULL COMMENT 'å“ç‰Œæè¿°',
  `sort_order` int(11) DEFAULT '0',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_tenant_name` (`tenant_id`,`brand_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å“ç‰Œè¡¨';
LOCK TABLES `product_brand` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'åˆ†ç±»ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT 'å•†æˆ·ID(0=å¹³å°çº§)',
  `parent_id` bigint(20) DEFAULT '0' COMMENT 'çˆ¶åˆ†ç±»ID',
  `category_name` varchar(50) NOT NULL,
  `icon` varchar(255) DEFAULT NULL COMMENT 'åˆ†ç±»å›¾æ ‡URL',
  `image` varchar(255) DEFAULT NULL,
  `level` tinyint(4) NOT NULL COMMENT 'å±‚çº§:1-ä¸€çº§,2-äºŒçº§,3-ä¸‰çº§',
  `sort_order` int(11) DEFAULT '0',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€:0-ç¦ç”¨,1-æ­£å¸¸',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_parent` (`tenant_id`,`parent_id`),
  KEY `idx_tenant_level` (`tenant_id`,`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†å“åˆ†ç±»è¡¨';
LOCK TABLES `product_category` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product_comment`;
CREATE TABLE `product_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'è¯„ä»·ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `order_item_id` bigint(20) NOT NULL COMMENT 'è®¢å•æ˜Žç»†ID',
  `member_id` bigint(20) NOT NULL COMMENT 'ä¼šå‘˜ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `content` varchar(500) NOT NULL COMMENT 'è¯„ä»·å†…å®¹',
  `images` varchar(1000) DEFAULT NULL COMMENT 'è¯„ä»·å›¾ç‰‡',
  `score` tinyint(4) NOT NULL DEFAULT '5' COMMENT 'è¯„åˆ†:1-5',
  `is_anonymous` tinyint(1) DEFAULT '0' COMMENT 'åŒ¿å',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€:0-éšè—,1-æ­£å¸¸',
  `reply_content` varchar(500) DEFAULT NULL,
  `reply_time` datetime DEFAULT NULL COMMENT 'å›žå¤æ—¶é—´',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†å“è¯„ä»·è¡¨';
LOCK TABLES `product_comment` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'å›¾ç‰‡ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `image_url` varchar(255) NOT NULL COMMENT 'å›¾ç‰‡URL',
  `sort_order` int(11) DEFAULT '0',
  `image_type` tinyint(4) DEFAULT '1' COMMENT 'ç±»åž‹:1-ä¸»å›¾,2-è¯¦æƒ…å›¾,3-SKUå›¾',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†å“å›¾ç‰‡è¡¨';
LOCK TABLES `product_image` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `spu_id` bigint(20) NOT NULL COMMENT 'SPU ID',
  `sku_name` varchar(200) DEFAULT NULL COMMENT 'SKUåç§°',
  `sku_code` varchar(50) DEFAULT NULL COMMENT 'SKUç¼–ç ',
  `image` varchar(255) DEFAULT NULL COMMENT 'SKUå›¾ç‰‡',
  `price` decimal(10,2) NOT NULL COMMENT 'é”€å”®ä»·æ ¼',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT 'åŽŸä»·',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT 'åº“å­˜',
  `lock_stock` int(11) DEFAULT '0',
  `sales` int(11) DEFAULT '0' COMMENT 'é”€é‡',
  `spec_values` varchar(500) DEFAULT NULL COMMENT 'è§„æ ¼å±žæ€§å€¼ID',
  `weight` decimal(10,2) DEFAULT NULL COMMENT 'é‡é‡(kg)',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€:0-ç¦ç”¨,1-æ­£å¸¸',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_sku_code` (`tenant_id`,`sku_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†å“SKUè¡¨';
LOCK TABLES `product_sku` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `category_id` bigint(20) NOT NULL COMMENT 'å¹³å°åˆ†ç±»ID',
  `brand_id` bigint(20) DEFAULT NULL COMMENT 'å“ç‰ŒID',
  `product_name` varchar(200) NOT NULL,
  `sub_title` varchar(200) DEFAULT NULL,
  `main_image` varchar(255) DEFAULT NULL COMMENT 'ä¸»å›¾URL',
  `images` varchar(1000) DEFAULT NULL COMMENT 'ç›¸å†Œå›¾ç‰‡URLåˆ—è¡¨',
  `description` text COMMENT 'PCç«¯æè¿°',
  `min_price` decimal(10,2) DEFAULT NULL,
  `max_price` decimal(10,2) DEFAULT NULL,
  `total_sales` int(11) DEFAULT '0' COMMENT 'æ€»é”€é‡',
  `total_stock` int(11) DEFAULT '0' COMMENT 'æ€»åº“å­˜',
  `status` tinyint(4) DEFAULT '0' COMMENT 'çŠ¶æ€:0-ä¸‹æž¶,1-ä¸Šæž¶,2-è¿è§„ä¸‹æž¶',
  `audit_status` tinyint(4) DEFAULT '0' COMMENT 'å®¡æ ¸:0-æ— éœ€,1-å¾…å®¡æ ¸,2-é€šè¿‡,3-æ‹’ç»',
  `audit_remark` varchar(200) DEFAULT NULL,
  `sort_order` int(11) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_status` (`tenant_id`,`status`),
  KEY `idx_total_sales` (`total_sales`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†å“SPUè¡¨';
LOCK TABLES `product_spu` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'åœ°åŒºID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT 'çˆ¶çº§ID',
  `name` varchar(50) NOT NULL COMMENT 'åœ°åŒºåç§°',
  `level` tinyint(4) NOT NULL COMMENT 'å±‚çº§:1-çœ,2-å¸‚,3-åŒº,4-ä¹¡é•‡,5-æ‘',
  `code` varchar(20) DEFAULT NULL COMMENT 'è¡Œæ”¿åŒºåˆ’ä»£ç ',
  `sort` int(11) DEFAULT '0' COMMENT 'æŽ’åº',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='åœ°åŒºè¡¨';
LOCK TABLES `region` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ç®¡ç†å‘˜ID',
  `username` varchar(50) NOT NULL COMMENT 'ç™»å½•è´¦å·',
  `password` varchar(100) NOT NULL COMMENT 'å¯†ç (bcrypt)',
  `real_name` varchar(50) DEFAULT NULL COMMENT 'çœŸå®žå§“å',
  `phone` varchar(20) DEFAULT NULL COMMENT 'æ‰‹æœºå·',
  `email` varchar(100) DEFAULT NULL COMMENT 'é‚®ç®±',
  `role_id` bigint(20) DEFAULT NULL COMMENT 'è§’è‰²ID',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€',
  `last_login_time` datetime DEFAULT NULL COMMENT 'æœ€åŽç™»å½•',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='ç³»ç»Ÿç®¡ç†å‘˜è¡¨';
LOCK TABLES `sys_admin` WRITE;
INSERT INTO `sys_admin` VALUES (1,'admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','è¶…çº§ç®¡ç†å‘˜',NULL,NULL,1,1,'2026-04-30 22:38:00','2026-04-30 09:39:09','2026-04-30 09:39:09',0);
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'é…ç½®ID',
  `config_key` varchar(100) NOT NULL COMMENT 'é…ç½®é”®',
  `config_value` text NOT NULL COMMENT 'é…ç½®å€¼',
  `config_group` varchar(50) DEFAULT NULL,
  `config_desc` varchar(200) DEFAULT NULL,
  `sort` int(11) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`config_group`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COMMENT='ç³»ç»Ÿé…ç½®è¡¨';
LOCK TABLES `sys_config` WRITE;
INSERT INTO `sys_config` VALUES (1,'site_name','B2B2Cå•†åŸŽ','basic','ç«™ç‚¹åç§°',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(2,'site_logo','','basic','ç«™ç‚¹Logo',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(3,'site_description','å¤šå•†æˆ·å•†åŸŽå¹³å°','basic','ç«™ç‚¹æè¿°',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(4,'order_auto_cancel_minutes','30','order','è®¢å•è‡ªåŠ¨å–æ¶ˆæ—¶é—´(åˆ†é’Ÿ)',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(5,'order_auto_confirm_days','7','order','å‘è´§åŽè‡ªåŠ¨ç¡®è®¤æ”¶è´§å¤©æ•°',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(6,'order_refund_days','7','order','ç¡®è®¤æ”¶è´§åŽå¯ç”³è¯·é€€æ¬¾å¤©æ•°',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(7,'default_commission_rate','5.00','settle','é»˜è®¤ä½£é‡‘æ¯”ä¾‹(%)',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(8,'settle_cycle_days','7','settle','ç»“ç®—å‘¨æœŸ(å¤©)',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(9,'min_withdraw_amount','100.00','settle','æœ€ä½ŽæçŽ°é‡‘é¢',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(10,'pay_wechat_enabled','1','pay','å¾®ä¿¡æ”¯ä»˜å¼€å…³',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(11,'pay_alipay_enabled','1','pay','æ”¯ä»˜å®æ”¯ä»˜å¼€å…³',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(12,'pay_balance_enabled','1','pay','ä½™é¢æ”¯ä»˜å¼€å…³',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(13,'sms_enabled','1','sms','çŸ­ä¿¡æœåŠ¡å¼€å…³',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(14,'upload_max_size','5','oss','ä¸Šä¼ æ–‡ä»¶æœ€å¤§å¤§å°(MB)',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(15,'upload_allowed_types','jpg,jpeg,png,gif,webp','oss','å…è®¸ä¸Šä¼ çš„æ–‡ä»¶ç±»åž‹',0,'2026-04-30 09:39:09','2026-04-30 09:39:09',0);
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'æ–‡ä»¶ID',
  `tenant_id` bigint(20) DEFAULT '0' COMMENT 'å•†æˆ·ID',
  `file_name` varchar(200) NOT NULL COMMENT 'åŽŸå§‹æ–‡ä»¶å',
  `file_path` varchar(500) NOT NULL COMMENT 'æ–‡ä»¶è·¯å¾„',
  `file_size` bigint(20) DEFAULT '0' COMMENT 'æ–‡ä»¶å¤§å°(å­—èŠ‚)',
  `file_type` varchar(50) DEFAULT NULL COMMENT 'æ–‡ä»¶ç±»åž‹',
  `url` varchar(500) NOT NULL COMMENT 'è®¿é—®URL',
  `biz_type` varchar(50) DEFAULT NULL COMMENT 'ä¸šåŠ¡ç±»åž‹',
  `biz_id` bigint(20) DEFAULT NULL COMMENT 'ä¸šåŠ¡ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='æ–‡ä»¶è®°å½•è¡¨';
LOCK TABLES `sys_file` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'æƒé™ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT 'çˆ¶æƒé™ID',
  `permission_name` varchar(50) NOT NULL,
  `permission_code` varchar(100) NOT NULL COMMENT 'æƒé™ç¼–ç ',
  `permission_type` tinyint(4) NOT NULL COMMENT 'ç±»åž‹:1-èœå•,2-æŒ‰é’®,3-æŽ¥å£',
  `path` varchar(200) DEFAULT NULL COMMENT 'èœå•è·¯ç”±',
  `icon` varchar(100) DEFAULT NULL COMMENT 'èœå•å›¾æ ‡',
  `sort_order` int(11) DEFAULT '0',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='æƒé™è¡¨';
LOCK TABLES `sys_permission` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'è§’è‰²ID',
  `role_name` varchar(50) NOT NULL COMMENT 'è§’è‰²åç§°',
  `role_code` varchar(50) NOT NULL COMMENT 'è§’è‰²ç¼–ç ',
  `description` varchar(200) DEFAULT NULL COMMENT 'æè¿°',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='è§’è‰²è¡¨';
LOCK TABLES `sys_role` WRITE;
INSERT INTO `sys_role` VALUES (1,'è¶…çº§ç®¡ç†å‘˜','SUPER_ADMIN','å¹³å°è¶…çº§ç®¡ç†å‘˜',1,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(2,'å¹³å°ç®¡ç†å‘˜','PLATFORM_ADMIN','å¹³å°æ™®é€šç®¡ç†å‘˜',1,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(3,'å•†æˆ·ä¸»ç®¡ç†å‘˜','MERCHANT_MAIN','å•†æˆ·ä¸»è´¦å·',1,'2026-04-30 09:39:09','2026-04-30 09:39:09',0),(4,'å•†æˆ·å­ç®¡ç†å‘˜','MERCHANT_SUB','å•†æˆ·å­è´¦å·',1,'2026-04-30 09:39:09','2026-04-30 09:39:09',0);
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'å…³è”ID',
  `role_id` bigint(20) NOT NULL COMMENT 'è§’è‰²ID',
  `permission_id` bigint(20) NOT NULL COMMENT 'æƒé™ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='è§’è‰²æƒé™å…³è”è¡¨';
LOCK TABLES `sys_role_permission` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'å•†æˆ·ID',
  `tenant_name` varchar(100) NOT NULL COMMENT 'å•†æˆ·åç§°',
  `logo` varchar(255) DEFAULT NULL COMMENT 'åº—é“ºLogo',
  `description` varchar(500) DEFAULT NULL COMMENT 'åº—é“ºæè¿°',
  `contact_name` varchar(50) NOT NULL COMMENT 'è”ç³»äººå§“å',
  `contact_phone` varchar(20) NOT NULL COMMENT 'è”ç³»äººæ‰‹æœºå·',
  `contact_email` varchar(100) DEFAULT NULL COMMENT 'è”ç³»äººé‚®ç®±',
  `business_license` varchar(255) DEFAULT NULL COMMENT 'è¥ä¸šæ‰§ç…§å›¾ç‰‡URL',
  `license_image` varchar(255) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL COMMENT 'è¯¦ç»†åœ°å€',
  `status` tinyint(4) DEFAULT '0' COMMENT 'çŠ¶æ€:0-å¾…å®¡æ ¸,1-æ­£å¸¸,2-ç¦ç”¨,3-å®¡æ ¸æ‹’ç»',
  `audit_remark` varchar(200) DEFAULT NULL,
  `commission_rate` decimal(5,2) DEFAULT '0.00' COMMENT 'å¹³å°ä½£é‡‘æ¯”ä¾‹(%)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='å•†æˆ·è¡¨';
LOCK TABLES `tenant` WRITE;
INSERT INTO `tenant` VALUES (1,'å¹³å°è‡ªè¥',NULL,NULL,'ç®¡ç†å‘˜','13800000000',NULL,NULL,NULL,NULL,1,NULL,0.00,'2026-04-30 09:39:09','2026-04-30 09:39:09',0);
UNLOCK TABLES;
DROP TABLE IF EXISTS `tenant_admin`;
CREATE TABLE `tenant_admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ç®¡ç†å‘˜ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `username` varchar(50) NOT NULL COMMENT 'ç™»å½•è´¦å·',
  `password` varchar(100) NOT NULL COMMENT 'å¯†ç (bcrypt)',
  `real_name` varchar(50) DEFAULT NULL COMMENT 'çœŸå®žå§“å',
  `phone` varchar(20) DEFAULT NULL COMMENT 'æ‰‹æœºå·',
  `email` varchar(100) DEFAULT NULL COMMENT 'é‚®ç®±',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'å¤´åƒ',
  `role_type` int(11) DEFAULT NULL,
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€:0-ç¦ç”¨,1-æ­£å¸¸',
  `last_login_time` datetime DEFAULT NULL COMMENT 'æœ€åŽç™»å½•æ—¶é—´',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT 'æœ€åŽç™»å½•IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_username` (`tenant_id`,`username`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='å•†æˆ·ç®¡ç†å‘˜è¡¨';
LOCK TABLES `tenant_admin` WRITE;
INSERT INTO `tenant_admin` VALUES (1,1,'self','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','è‡ªè¥ç®¡ç†å‘˜',NULL,NULL,NULL,NULL,1,'2026-04-30 22:38:00',NULL,'2026-04-30 09:39:09','2026-04-30 09:39:09',0);
UNLOCK TABLES;
DROP TABLE IF EXISTS `tenant_apply`;
CREATE TABLE `tenant_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ç”³è¯·ID',
  `tenant_name` varchar(100) NOT NULL COMMENT 'å•†æˆ·åç§°',
  `contact_name` varchar(50) NOT NULL COMMENT 'è”ç³»äºº',
  `contact_phone` varchar(20) NOT NULL COMMENT 'æ‰‹æœºå·',
  `contact_email` varchar(100) DEFAULT NULL COMMENT 'é‚®ç®±',
  `business_license` varchar(255) NOT NULL COMMENT 'è¥ä¸šæ‰§ç…§URL',
  `license_image` varchar(255) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `apply_status` int(11) DEFAULT '0',
  `audit_remark` varchar(200) DEFAULT NULL,
  `audit_user_id` bigint(20) DEFAULT NULL COMMENT 'å®¡æ ¸äººID',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT 'å®¡æ ¸é€šè¿‡åŽå•†æˆ·ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`apply_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†æˆ·å…¥é©»ç”³è¯·è¡¨';
LOCK TABLES `tenant_apply` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tenant_category`;
CREATE TABLE `tenant_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'åˆ†ç±»ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT 'çˆ¶åˆ†ç±»ID',
  `name` varchar(50) NOT NULL COMMENT 'åˆ†ç±»åç§°',
  `icon` varchar(255) DEFAULT NULL COMMENT 'åˆ†ç±»å›¾æ ‡',
  `sort` int(11) DEFAULT '0' COMMENT 'æŽ’åº',
  `status` tinyint(4) DEFAULT '1' COMMENT 'çŠ¶æ€:0-ç¦ç”¨,1-æ­£å¸¸',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`parent_id`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†æˆ·åˆ†ç±»è¡¨';
LOCK TABLES `tenant_category` WRITE;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tenant_settle`;
CREATE TABLE `tenant_settle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ç»“ç®—ID',
  `tenant_id` bigint(20) NOT NULL COMMENT 'å•†æˆ·ID',
  `settle_no` varchar(50) NOT NULL COMMENT 'ç»“ç®—å•å·',
  `order_amount` decimal(10,2) NOT NULL,
  `commission_amount` decimal(10,2) NOT NULL,
  `settle_amount` decimal(10,2) NOT NULL,
  `status` tinyint(4) DEFAULT '0' COMMENT 'çŠ¶æ€:0-å¾…ç»“ç®—,1-å·²ç»“ç®—,2-å·²æ‹’ç»',
  `period_start` datetime DEFAULT NULL,
  `period_end` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint(4) DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settle_no` (`settle_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='å•†æˆ·ç»“ç®—è¡¨';
LOCK TABLES `tenant_settle` WRITE;
UNLOCK TABLES;
