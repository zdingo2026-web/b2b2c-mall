# 第一期MVP闭环 - 验收报告

> 版本：v1.0 | 日期：2026-04-30 | 状态：通过

---

## 一、验收结论

**第一期MVP后端核心功能验收通过**，可部署运行。前端骨架已搭建，页面功能待第二期完善。

| 维度 | 状态 | 说明 |
|------|------|------|
| 后端API | PASS | 22/22 API冒烟测试通过 |
| 单元测试 | PASS | 89个测试，0失败，BUILD SUCCESS |
| 安全修复 | PASS | 3个CRITICAL已修复 |
| 基础设施 | PASS | MySQL/Redis/RabbitMQ运行正常 |
| 管理后台前端 | PASS | 编译通过，dev server运行 |
| 移动端H5 | PASS | 编译通过，dev server运行 |

---

## 二、后端API验收结果

### 2.1 会员认证 (4/4 PASS)

| API | 方法 | 路径 | 结果 |
|-----|------|------|------|
| 注册 | POST | /api/v1/member/auth/register | PASS |
| 登录 | POST | /api/v1/member/auth/login | PASS |
| 刷新Token | POST | /api/v1/member/auth/refresh | PASS |
| 获取个人信息 | GET | /api/v1/member/member/info | PASS |

### 2.2 平台管理 (8/8 PASS)

| API | 方法 | 路径 | 结果 |
|-----|------|------|------|
| 平台登录 | POST | /api/v1/platform/auth/login | PASS |
| 商户列表 | GET | /api/v1/platform/tenant/list | PASS |
| 会员列表 | GET | /api/v1/platform/member/list | PASS |
| 商品列表 | GET | /api/v1/platform/product/spu/list | PASS |
| 订单列表 | GET | /api/v1/platform/order/list | PASS |
| 数据统计 | GET | /api/v1/platform/statistics/overview | PASS |
| 分类树 | GET | /api/v1/platform/product/category/tree | PASS |
| 品牌列表 | GET | /api/v1/platform/product/brand/list | PASS |

### 2.3 商户端 (3/3 PASS)

| API | 方法 | 路径 | 结果 |
|-----|------|------|------|
| 商户登录 | POST | /api/v1/merchant/auth/login | PASS |
| 商户商品列表 | GET | /api/v1/merchant/product/spu/list | PASS |
| 商户订单列表 | GET | /api/v1/merchant/order/list | PASS |

### 2.4 C端会员API (5/5 PASS)

| API | 方法 | 路径 | 结果 |
|-----|------|------|------|
| 首页 | GET | /api/v1/member/home | PASS |
| 购物车列表 | GET | /api/v1/member/cart/list | PASS |
| 商品列表 | GET | /api/v1/member/product/spu/list | PASS |
| 地址列表 | GET | /api/v1/member/address/list | PASS |
| 地区树 | GET | /api/v1/common/region/tree | PASS |

### 2.5 API文档 (2/2 PASS)

| 端 | URL | 结果 |
|----|-----|------|
| C端API文档 | http://localhost:8080/doc.html | PASS (HTTP 200) |
| Admin API文档 | http://localhost:8082/doc.html | PASS (HTTP 200) |

---

## 三、单元测试验收结果

| 测试类 | 测试数 | 状态 |
|--------|--------|------|
| OrderStateMachineTest | 19 | PASS |
| OrderServiceTest | 11 | PASS |
| CartServiceTest | 15 | PASS |
| StockServiceTest | 9 | PASS |
| BalancePayStrategyTest | 6 | PASS |
| PaymentServiceTest | 5 | PASS |
| AuthServiceTest | 15 | PASS |
| SmsServiceTest | 9 | PASS |
| **合计** | **89** | **BUILD SUCCESS** |

---

## 四、安全修复记录

### CRITICAL级别 (3/3 已修复)

| 编号 | 问题 | 修复方案 | 文件 |
|------|------|---------|------|
| C-01 | JWT密钥硬编码 | 移除默认值，添加@PostConstruct验证 | JwtUtil.java |
| C-02 | Token黑名单未校验 | 添加Redis黑名单检查 | JwtAuthenticationFilter.java, SecurityConfig.java |
| C-03 | SQL拼接注入风险 | 改用参数化SQL | StockService.java, ProductSkuMapper.java, ProductSpuMapper.java |

### HIGH级别 (待第二期修复)

| 编号 | 问题 | 计划 |
|------|------|------|
| H-01 | CSRF已禁用 | 第二期评估启用 |
| H-02 | 公开端点过多 | 已收紧SecurityConfig |
| H-03 | ThreadLocal异步风险 | 第二期添加TransmittableThreadLocal |
| H-04 | 支付幂等性 | 第二期完善 |
| H-05 | 登录速率限制 | 第二期添加 |

---

## 五、部署信息

### 5.1 访问链接

| 端 | URL | 说明 |
|----|-----|------|
| C端API | http://localhost:8080 | 会员端后端API |
| C端API文档 | http://localhost:8080/doc.html | Knife4j接口文档 |
| Admin API | http://localhost:8082 | 管理端后端API |
| Admin API文档 | http://localhost:8082/doc.html | Knife4j接口文档 |
| 管理后台前端 | http://localhost:5174 | Vue3 + ElementPlus |
| 移动端H5 | http://localhost:3002 | UniApp Vue3 |

### 5.2 测试账号

| 角色 | 用户名 | 密码 | 端口 |
|------|--------|------|------|
| 平台管理员 | admin | 123456 | 8082 |
| 商户管理员 | self | 123456 | 8082 |
| 会员 | 手机号注册 | 自定义 | 8080 |

### 5.3 基础设施

| 服务 | 端口 | 账号 | 密码 |
|------|------|------|------|
| MySQL 5.7 | 3306 | root | root123 |
| Redis 6 | 6379 | 无 | 无 |
| RabbitMQ 3 | 5672/15672 | mall | mall123 |

### 5.4 数据库

- 数据库名：mall_b2b2c
- 表数量：36张
- 字符集：utf8mb4

---

## 六、已修复的部署问题

| 问题 | 原因 | 修复 |
|------|------|------|
| RabbitMQ连接失败 | Docker凭证与配置不匹配 | 统一为mall/mall123 |
| Knife4j NPE | Spring Boot 2.7路径匹配策略 | 添加ant_path_matcher |
| 会员注册SQL错误 | member表无tenant_id | 添加到ignoreTable |
| 平台管理员查询报错 | tenant_id=0过滤所有数据 | ignoreTable检查平台角色 |
| 商户登录失败 | 未认证请求也添加tenant条件 | null时跳过租户过滤 |
| Fat JAR仅23KB | 未配置repackage goal | 添加spring-boot-maven-plugin |
| 实体与数据库不匹配 | init.sql与Entity定义不同步 | ALTER TABLE对齐27张表 |
| 移动端H5返回404 | history路由+index.html路径 | 切换hash模式，修正script src |

---

## 七、待第二期完善

1. 管理后台前端页面功能完善（商品管理、订单管理等）
2. 移动端前端页面功能完善（搜索、下单、支付等）
3. HIGH级别安全修复（CSRF、速率限制等）
4. 集成测试和E2E测试
5. Docker全栈部署（docker-compose up一键启动）
6. CI/CD流水线配置
