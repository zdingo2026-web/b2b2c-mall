-- ============================================================
-- B2B2C商城 V5 数据库迁移脚本
-- 新增短信验证码表(sms_code)
-- 执行前提: 已执行 init_fixed.sql + v2/v3/v4 迁移脚本
-- ============================================================

CREATE TABLE IF NOT EXISTS sms_code (
  id BIGINT PRIMARY KEY COMMENT '主键ID(snowflake)',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  code VARCHAR(10) NOT NULL COMMENT '验证码',
  biz_type TINYINT NOT NULL DEFAULT 1 COMMENT '业务类型 1-登录 2-注册 3-修改密码 4-绑定手机',
  ip VARCHAR(50) COMMENT '请求IP',
  expire_at DATETIME NOT NULL COMMENT '过期时间',
  verified TINYINT NOT NULL DEFAULT 0 COMMENT '是否已验证 0-未验证 1-已验证',
  verify_attempts INT NOT NULL DEFAULT 0 COMMENT '验证尝试次数',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_phone_biz (phone, biz_type),
  INDEX idx_expire (expire_at),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码记录表';
