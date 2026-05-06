-- ============================================================
-- B2B2C商城系统 - 订单补充数据（订单项、地址、支付记录）
-- 在full_mock_data.sql之后执行
-- ============================================================
USE `mall_b2b2c`;

-- ----------------------------
-- 获取会员ID变量（与full_mock_data.sql一致）
-- ----------------------------
SET @m1 = (SELECT id FROM member WHERE phone='13800138001' LIMIT 1);
SET @m2 = (SELECT id FROM member WHERE phone='13900139001' LIMIT 1);
SET @m3 = (SELECT id FROM member WHERE phone='13700137001' LIMIT 1);
SET @m4 = (SELECT id FROM member WHERE phone='13600136001' LIMIT 1);

-- ----------------------------
-- 补充更多订单（10条待收货，凑够100条）
-- ----------------------------
INSERT INTO `order_main` (`tenant_id`, `order_no`, `parent_id`, `order_type`, `member_id`, `total_amount`, `freight_amount`, `discount_amount`, `pay_amount`, `pay_type`, `pay_time`, `order_status`, `pay_status`, `delivery_type`, `logistics_no`, `logistics_company`, `delivery_time`, `remark`, `expire_time`) VALUES
(0, 'ORD2024020600001', 0, 1, @m1, 7999.00, 0.00, 0.00, 7999.00, 2, '2024-02-06 10:00:00', 2, 1, 1, 'SF22345678901', '顺丰速运', '2024-02-07 10:00:00', '', '2024-02-06 10:30:00'),
(0, 'ORD2024020600002', 0, 1, @m2, 6499.00, 0.00, 0.00, 6499.00, 1, '2024-02-06 11:00:00', 2, 1, 1, 'SF22345678902', '顺丰速运', '2024-02-07 11:00:00', '', '2024-02-06 11:30:00'),
(0, 'ORD2024020600003', 0, 1, @m3, 4999.00, 0.00, 0.00, 4999.00, 2, '2024-02-06 12:00:00', 2, 1, 1, 'YD22345678901', '韵达快递', '2024-02-07 12:00:00', '', '2024-02-06 12:30:00'),
(0, 'ORD2024020600004', 0, 1, @m4, 8999.00, 0.00, 0.00, 8999.00, 1, '2024-02-06 13:00:00', 2, 1, 1, 'YD22345678902', '韵达快递', '2024-02-07 13:00:00', '', '2024-02-06 13:30:00'),
(0, 'ORD2024020600005', 0, 1, @m1, 9999.00, 0.00, 0.00, 9999.00, 2, '2024-02-06 14:00:00', 2, 1, 1, 'ZTO22345678901', '中通快递', '2024-02-07 14:00:00', '', '2024-02-06 14:30:00'),
(0, 'ORD2024020600006', 0, 1, @m2, 8999.00, 0.00, 0.00, 8999.00, 1, '2024-02-06 15:00:00', 2, 1, 1, 'ZTO22345678902', '中通快递', '2024-02-07 15:00:00', '', '2024-02-06 15:30:00'),
(0, 'ORD2024020600007', 0, 1, @m3, 7499.00, 0.00, 0.00, 7499.00, 2, '2024-02-06 16:00:00', 2, 1, 1, 'STO22345678901', '申通快递', '2024-02-07 16:00:00', '', '2024-02-06 16:30:00'),
(0, 'ORD2024020600008', 0, 1, @m4, 6999.00, 0.00, 0.00, 6999.00, 1, '2024-02-06 17:00:00', 2, 1, 1, 'STO22345678902', '申通快递', '2024-02-07 17:00:00', '', '2024-02-06 17:30:00'),
(0, 'ORD2024020600009', 0, 1, @m1, 5499.00, 0.00, 0.00, 5499.00, 2, '2024-02-06 18:00:00', 2, 1, 1, 'JD22345678901', '京东物流', '2024-02-07 18:00:00', '', '2024-02-06 18:30:00'),
(0, 'ORD2024020600010', 0, 1, @m2, 3999.00, 0.00, 0.00, 3999.00, 1, '2024-02-06 19:00:00', 2, 1, 1, 'JD22345678902', '京东物流', '2024-02-07 19:00:00', '', '2024-02-06 19:30:00');

-- ----------------------------
-- 为所有订单生成订单项
-- ----------------------------
INSERT INTO `order_item` (`tenant_id`, `order_id`, `spu_id`, `sku_id`, `product_name`, `price`, `quantity`, `subtotal`, `pay_amount`)
SELECT
  1,
  o.id,
  CASE FLOOR(RAND() * 13) + 1
    WHEN 1 THEN 1 WHEN 2 THEN 2 WHEN 3 THEN 3 WHEN 4 THEN 4 WHEN 5 THEN 5
    WHEN 6 THEN 6 WHEN 7 THEN 7 WHEN 8 THEN 8 WHEN 9 THEN 9 WHEN 10 THEN 10
    WHEN 11 THEN 11 WHEN 12 THEN 12 ELSE 13
  END,
  CASE FLOOR(RAND() * 13) + 1
    WHEN 1 THEN 1 WHEN 2 THEN 2 WHEN 3 THEN 3 WHEN 4 THEN 4 WHEN 5 THEN 5
    WHEN 6 THEN 6 WHEN 7 THEN 7 WHEN 8 THEN 8 WHEN 9 THEN 9 WHEN 10 THEN 10
    WHEN 11 THEN 11 WHEN 12 THEN 12 ELSE 13
  END,
  CASE FLOOR(RAND() * 13) + 1
    WHEN 1 THEN 'iPhone 15 Pro' WHEN 2 THEN '华为Mate 60 Pro' WHEN 3 THEN '小米14 Pro' WHEN 4 THEN '三星Galaxy S24 Ultra' WHEN 5 THEN '联想拯救者Y9000P'
    WHEN 6 THEN '戴尔XPS 13 Plus' WHEN 7 THEN '小米Redmi G Pro' WHEN 8 THEN '格力云佳3匹空调' WHEN 9 THEN '美的508升冰箱' WHEN 10 THEN '海尔10公斤洗衣机'
    WHEN 11 THEN '伊利金典纯牛奶' WHEN 12 THEN '蒙牛特仑苏纯牛奶' ELSE '农夫山泉饮用天然水'
  END,
  CASE FLOOR(RAND() * 13) + 1
    WHEN 1 THEN 7999.00 WHEN 2 THEN 6499.00 WHEN 3 THEN 4999.00 WHEN 4 THEN 8999.00 WHEN 5 THEN 9999.00
    WHEN 6 THEN 8999.00 WHEN 7 THEN 7499.00 WHEN 8 THEN 6999.00 WHEN 9 THEN 5499.00 WHEN 10 THEN 3999.00
    WHEN 11 THEN 59.90 WHEN 12 THEN 65.90 ELSE 36.90
  END,
  1,
  CASE FLOOR(RAND() * 13) + 1
    WHEN 1 THEN 7999.00 WHEN 2 THEN 6499.00 WHEN 3 THEN 4999.00 WHEN 4 THEN 8999.00 WHEN 5 THEN 9999.00
    WHEN 6 THEN 8999.00 WHEN 7 THEN 7499.00 WHEN 8 THEN 6999.00 WHEN 9 THEN 5499.00 WHEN 10 THEN 3999.00
    WHEN 11 THEN 59.90 WHEN 12 THEN 65.90 ELSE 36.90
  END,
  CASE FLOOR(RAND() * 13) + 1
    WHEN 1 THEN 7999.00 WHEN 2 THEN 6499.00 WHEN 3 THEN 4999.00 WHEN 4 THEN 8999.00 WHEN 5 THEN 9999.00
    WHEN 6 THEN 8999.00 WHEN 7 THEN 7499.00 WHEN 8 THEN 6999.00 WHEN 9 THEN 5499.00 WHEN 10 THEN 3999.00
    WHEN 11 THEN 59.90 WHEN 12 THEN 65.90 ELSE 36.90
  END
FROM order_main o;

-- ----------------------------
-- 为所有订单生成地址快照
-- ----------------------------
INSERT INTO `order_address` (`order_id`, `receiver_name`, `receiver_phone`, `province_name`, `city_name`, `district_name`, `detail_address`)
SELECT
  o.id,
  CASE FLOOR(RAND() * 10)
    WHEN 0 THEN '张伟' WHEN 1 THEN '王芳' WHEN 2 THEN '李伟' WHEN 3 THEN '赵勇' WHEN 4 THEN '钱艳'
    WHEN 5 THEN '孙军' WHEN 6 THEN '周丽' WHEN 7 THEN '吴强' WHEN 8 THEN '郑燕' ELSE '王磊'
  END,
  CONCAT('138', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
  CASE FLOOR(RAND() * 5)
    WHEN 0 THEN '北京市' WHEN 1 THEN '上海市' WHEN 2 THEN '广东省' WHEN 3 THEN '江苏省' ELSE '浙江省'
  END,
  CASE FLOOR(RAND() * 5)
    WHEN 0 THEN '北京市' WHEN 1 THEN '上海市' WHEN 2 THEN '广州市' WHEN 3 THEN '深圳市' ELSE '杭州市'
  END,
  CASE FLOOR(RAND() * 5)
    WHEN 0 THEN '朝阳区' WHEN 1 THEN '徐汇区' WHEN 2 THEN '天河区' WHEN 3 THEN '南山区' ELSE '西湖区'
  END,
  CONCAT(CASE FLOOR(RAND() * 10)
    WHEN 0 THEN '中关村大街' WHEN 1 THEN '淮海中路' WHEN 2 THEN '天河路' WHEN 3 THEN '科技园' WHEN 4 THEN '文三路'
    WHEN 5 THEN '长安街' WHEN 6 THEN '南京路' WHEN 7 THEN '珠江路' WHEN 8 THEN '解放路' ELSE '中山路'
  END, ' ', FLOOR(RAND() * 1000), '号')
FROM order_main o;

-- ----------------------------
-- 为已支付的订单生成支付记录
-- ----------------------------
INSERT INTO `payment_record` (`payment_no`, `order_id`, `order_no`, `member_id`, `pay_type`, `pay_amount`, `status`, `trade_no`, `pay_time`, `expire_time`)
SELECT
  CONCAT('PAY', SUBSTRING(o.order_no, 4)),
  o.id,
  o.order_no,
  o.member_id,
  IFNULL(o.pay_type, 1),
  o.pay_amount,
  CASE o.order_status WHEN 4 THEN 3 WHEN 5 THEN 2 ELSE 1 END,
  CONCAT(CASE IFNULL(o.pay_type, 1) WHEN 1 THEN 'WECHAT' ELSE 'ALIPAY' END, FLOOR(RAND() * 1000000000000)),
  o.pay_time,
  o.expire_time
FROM order_main o WHERE o.pay_status != 0;
