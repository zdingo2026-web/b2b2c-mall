# 商户管理后台验证报告

## 1. 商户管理账号和密码

### 测试账号一览

| 角色 | 用户名 | 密码 | Tenant ID | 说明 |
|------|--------|------|-----------|------|
| 平台管理员 | `admin` | `123456` | 0 | 系统超级管理员 |
| 自营商户 | `self` | `123456` | 1 | 平台自营店铺 |
| 数码商户 | `digital` | `123456` | 2 | 电子数码旗舰店 |
| 服装商户 | `fashion` | `123456` | 3 | 服装服饰专营店 |
| 家居商户 | `home` | `123456` | 4 | 家居生活馆 |
| 食品商户 | `food` | `123456` | 5 | 食品超市 |
| 母婴商户 | `baby` | `123456` | 6 | 母婴用品店 |

> 商户端登录入口: `POST /api/v1/merchant/auth/login`，用 form params（非 JSON body）

---

## 2. PRD 要求 vs 当前实现 对比

### 商户端功能清单（PRD M-01 ~ M-10）

| 编号 | PRD 功能 | 优先级 | 后端API | 前端页面 | 状态 | 差距说明 |
|------|---------|--------|---------|---------|------|---------|
| M-01 | 商户登录 | P0 | ✅ login/logout | ✅ 登录页 | **完成** | - |
| M-02 | 首页工作台 | P0 | ✅ /merchant/statistics/overview | ✅ dashboard | **完成** | - |
| M-03 | 商品管理 | P0 | ✅ SPU/SKU CRUD + 上下架 | ✅ 商品列表+发布页 | **基本完成** | 缺少"批量上下架"；商品分类为商户独立分类（已有）但品牌列表用的是平台接口，商户端应也可用 |
| M-04 | 订单管理 | P0 | ✅ /merchant/order/list, /{orderNo} | ✅ 订单管理页 | **完成** | - |
| M-05 | 发货管理 | P0 | ✅ /merchant/order/item/{id}/ship | ✅ 发货对话框 | **完成** | - |
| M-06 | 退换货管理 | P0 | ✅ /merchant/order/refund/list, /{id}/handle | ✅ 退款处理（在资金管理页） | **完成** | 退款列表放在了资金管理页而非独立页面，功能可用但组织与PRD略不同 |
| M-07 | 店铺设置 | P0 | ❌ 缺少更新API | ✅ 表单页（但保存是TODO） | **未完成** | 前端有表单UI，但后端无商户端更新店铺信息API，保存按钮未接入 |
| M-08 | 子管理员管理 | P1 | ❌ 无 | ❌ 无页面 | **未实现** | PRD P1优先级，数据库有tenant_admin表支持 |
| M-09 | 资金管理 | P1 | 部分（退款API有） | ✅ 资金管理页 | **部分完成** | 无账户余额/提现申请/资金明细API，仅有退款处理 |
| M-10 | 数据统计 | P1 | ✅ /merchant/statistics/overview | ✅ dashboard含统计 | **部分完成** | 仅有概览统计，无详细数据分析 |

### 前端路由/菜单现状

| 菜单 | 路由 | 状态 |
|------|------|------|
| 工作台 | /merchant/dashboard | ✅ |
| 商品列表 | /merchant/product/list | ✅ |
| 发布商品 | /merchant/product/add | ✅ |
| 订单管理 | /merchant/order | ✅ |
| 店铺设置 | /merchant/shop | ✅（保存功能未接入） |
| 资金管理 | /merchant/finance | ✅（退款处理有，余额/提现无） |

### PRD 要求但缺失的菜单

| 缺失菜单 | PRD编号 | 优先级 |
|----------|---------|--------|
| 子管理员管理 | M-08 | P1 |
| 资金明细 | M-09 | P1 |
| 数据统计（详细） | M-10 | P1 |

---

## 3. 差异分析与修改建议

### P0 问题（必须修复）

#### 3.1 店铺设置保存功能缺失

**现状**: 前端 `shop/index.vue` 有表单（店铺名称、Logo、描述），但保存按钮是 TODO，未调用后端 API。

**需要**:
- 后端: 在 `MerchantAuthController` 或新建 `MerchantShopController` 添加 `PUT /api/v1/merchant/shop` 更新店铺信息
- 前端: 接入更新 API，替换 TODO

#### 3.2 商户端缺少独立的店铺信息查询API

**现状**: 前端店铺设置页调用 `getTenantDetail(tenantId)`，但这个是平台端接口（`/api/v1/platform/tenant/{id}`），商户端 JWT 中有 tenantId，应该用商户端自己的接口获取。

**需要**: 添加 `GET /api/v1/merchant/shop` 返回当前商户店铺信息

### P1 问题（应该修复）

#### 3.3 子管理员管理缺失（M-08）

**现状**: 完全没有实现。数据库 `tenant_admin` 表已支持多管理员。

**需要**:
- 后端: 新建 `MerchantAdminController`，提供子管理员 CRUD（`/api/v1/merchant/admin/list`, `POST`, `PUT/{id}`, `DELETE/{id}`）
- 前端: 新建 `/merchant/admin` 页面
- 权限: 只有主管理员（role_type 为主管理员）可以管理子管理员

#### 3.4 资金管理不完整（M-09）

**现状**: 只有退款处理，缺少账户余额、提现申请、资金明细。

**需要**:
- 后端: 新增商户账户余额查询、提现申请、资金流水列表 API
- 数据库: `tenant_settle` 表已存在，可复用
- 前端: 完善资金管理页面

#### 3.5 数据统计不够详细（M-10）

**现状**: dashboard 只有概览数据（今日成交额/订单数/客单价）。

**需要**:
- 后端: 增加趋势数据 API（近7/30天成交趋势、商品销量排行等）
- 前端: 增加图表展示

### 可选优化

#### 3.6 退换货管理独立页面

**现状**: 退款处理放在资金管理页，与PRD描述的"退换货管理"独立模块不同。

**建议**: 将退换货从资金管理页拆出，作为独立菜单项 `/merchant/refund`

#### 3.7 商品批量上下架

**现状**: 只有单个商品上下架。

**建议**: 增加批量操作接口和前端多选功能

---

## 4. 修改计划

### Phase 1: P0 修复 — 店铺设置功能（预估 1-2h）

1. **后端**: 新建 `MerchantShopController`
   - `GET /api/v1/merchant/shop` — 获取当前商户店铺信息（从 JWT 取 tenantId）
   - `PUT /api/v1/merchant/shop` — 更新店铺信息（名称、Logo、描述等）
2. **前端**: 修改 `shop/index.vue`
   - 替换 `getTenantDetail(tenantId)` 为 `getMerchantShop()`
   - 保存按钮接入 `updateMerchantShop()` API
3. **前端 API**: 在 `api/tenant.ts` 添加商户端店铺 API

### Phase 2: P1 修复 — 子管理员管理（预估 2-3h）

1. **后端**: 新建 `MerchantAdminController`
   - `GET /api/v1/merchant/admin/list` — 子管理员列表
   - `POST /api/v1/merchant/admin` — 添加子管理员
   - `PUT /api/v1/merchant/admin/{id}` — 编辑子管理员
   - `PUT /api/v1/merchant/admin/{id}/status` — 启用/禁用
   - `DELETE /api/v1/merchant/admin/{id}` — 删除
2. **前端**: 新建 `/merchant/admin` 页面 + 路由
3. **前端 API**: 新增 `api/merchant-admin.ts`

### Phase 3: P1 修复 — 资金管理完善（预估 2-3h）

1. **后端**: 新建 `MerchantFinanceController`
   - `GET /api/v1/merchant/finance/balance` — 账户余额
   - `GET /api/v1/merchant/finance/settle/list` — 结算记录
   - `POST /api/v1/merchant/finance/withdraw` — 提现申请
   - `GET /api/v1/merchant/finance/flow` — 资金流水
2. **前端**: 完善资金管理页面

### Phase 4: 优化 — 退换货独立页面 + 数据统计增强（预估 2-3h）

1. 退换货管理从资金管理页拆出
2. 数据统计增加趋势图表
