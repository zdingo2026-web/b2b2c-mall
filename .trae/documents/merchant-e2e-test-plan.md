# 商户管理后台E2E测试执行计划

## 1. 测试概述

### 1.1 测试目标
对商户管理后台的所有功能进行完整的端到端测试，使用已有的Playwright测试套件。

### 1.2 测试环境
- **前端地址**: http://localhost:5174
- **测试账号**: digital/123456 (商户账号)
- **测试框架**: Playwright
- **浏览器**: Chromium (默认)

### 1.3 测试覆盖模块

| 模块 | 测试文件 | 测试用例数 |
|------|----------|-----------|
| 商户工作台 | merchant-dashboard.spec.ts | 6 |
| 商户商品管理 | merchant-product.spec.ts | 9 |
| 商户订单管理 | merchant-order.spec.ts | 6 |
| 商户店铺设置 | merchant-shop.spec.ts | 5 |
| 商户资金管理 | merchant-finance.spec.ts | 6 |
| 商户退换货管理 | merchant-refund.spec.ts | 5 |
| **总计** | **6个文件** | **37个测试用例** |

## 2. 前置条件检查

### 2.1 服务状态检查
- [ ] 后端服务 (mall-admin) 是否运行在 8082 端口
- [ ] 前端服务 (mall-admin-web) 是否运行在 5174 端口
- [ ] 数据库是否可用且有测试数据
- [ ] Redis 是否正常运行

### 2.2 测试环境准备
- [ ] Playwright 依赖已安装 (npm install 在 e2e 目录)
- [ ] 浏览器已存在 (不执行 playwright install)
- [ ] e2e/screenshots 目录已存在
- [ ] 商户测试账号 (digital/123456) 可用

## 3. 测试执行步骤

### 3.1 修改配置（如果需要）
如果需要使用非headless模式观察浏览器行为，修改 `e2e/playwright.config.ts`:
```typescript
use: {
  baseURL: 'http://localhost:5174',
  headless: false,  // 改为false可观察浏览器行为
  screenshot: 'on',
  video: 'retain-on-failure',
  trace: 'retain-on-failure',
  actionTimeout: 10000,
}
```

### 3.2 执行测试

#### 方式一：运行所有商户相关测试
```bash
cd e2e
npm test -- --grep="商户"
```

#### 方式二：逐个模块运行
```bash
# 1. 商户工作台
cd e2e
npx playwright test tests/merchant-dashboard.spec.ts --reporter=list

# 2. 商户商品管理
npx playwright test tests/merchant-product.spec.ts --reporter=list

# 3. 商户订单管理
npx playwright test tests/merchant-order.spec.ts --reporter=list

# 4. 商户店铺设置
npx playwright test tests/merchant-shop.spec.ts --reporter=list

# 5. 商户资金管理
npx playwright test tests/merchant-finance.spec.ts --reporter=list

# 6. 商户退换货管理
npx playwright test tests/merchant-refund.spec.ts --reporter=list
```

#### 方式三：使用UI模式运行（调试用）
```bash
cd e2e
npm run test:ui
```

### 3.3 测试执行注意事项
- 测试默认在headless模式运行，如需观察浏览器行为，修改配置中的 `headless: false`
- 每个测试都会自动截图，保存在 `e2e/screenshots/` 目录
- 失败测试会保留视频和trace信息
- 测试会自动以商户账号登录，无需手动操作

## 4. 测试报告查看

### 4.1 实时报告
测试执行过程中会在终端显示测试进度和结果。

### 4.2 HTML报告
测试完成后，生成HTML报告：
```bash
cd e2e
npm run report
```
这会打开浏览器显示 `playwright-report/index.html`。

### 4.3 测试产物
- **截图**: `e2e/screenshots/` 目录
- **测试结果**: `e2e/test-results/` 目录
- **HTML报告**: `e2e/playwright-report/` 目录

## 5. 预期测试覆盖内容

### 5.1 商户工作台
- ✅ 页面加载验证
- ✅ 统计数据展示（今日成交额、今日订单数、今日客单价、累计成交额）
- ✅ 近7天趋势图表
- ✅ 商品销量排行
- ✅ 待处理事项展示
- ✅ 待处理事项跳转功能

### 5.2 商户商品管理
- ✅ 页面加载验证
- ✅ 关键词搜索功能
- ✅ 分类筛选功能
- ✅ 状态筛选功能（草稿、待审核、已上架、已下架）
- ✅ 商品上架功能
- ✅ 商品下架功能
- ✅ 编辑商品功能
- ✅ 跳转发布商品页面
- ✅ 分页功能

### 5.3 商户订单管理
- ✅ 页面加载验证
- ✅ 订单号搜索功能
- ✅ 订单状态筛选（待付款、待发货、待收货、已完成、已取消、待评价）
- ✅ 查看订单详情功能
- ✅ 订单发货功能（表单验证）
- ✅ 分页功能

### 5.4 商户店铺设置
- ✅ 页面加载验证
- ✅ 表单字段展示验证
- ✅ 店铺信息编辑验证
- ✅ 保存按钮功能验证
- ✅ 分隔线和联系信息展示

### 5.5 商户资金管理
- ✅ 页面加载验证
- ✅ 资金统计卡片验证（可用余额、累计结算、待提现）
- ✅ 结算记录表格验证
- ✅ 申请提现按钮验证
- ✅ 申请提现对话框验证
- ✅ 分页功能

### 5.6 商户退换货管理
- ✅ 页面加载验证
- ✅ 表格数据展示验证
- ✅ 同意退款功能测试
- ✅ 拒绝退款功能测试
- ✅ 分页功能

## 6. 测试结果记录模板

### 6.1 测试总结
```
测试日期: YYYY-MM-DD HH:MM
总测试用例: 37
通过: X
失败: Y
跳过: Z
通过率: XX%
执行时长: Xm Ys
```

### 6.2 失败测试详情
| 测试文件 | 测试名称 | 失败原因 | 截图 |
|----------|---------|---------|------|
| merchant-xxx.spec.ts | xxx测试 | 错误信息 | screenshots/xxx.png |

## 7. 风险与注意事项

### 7.1 可能的问题
1. **数据依赖**: 部分测试需要存在测试数据（如订单、商品），如果数据库为空可能导致测试失败
2. **网络延迟**: 测试中的waitForTimeout可能需要根据实际环境调整
3. **服务状态**: 如果后端服务未启动，所有测试都会失败

### 7.2 应对措施
1. 执行前检查所有服务状态
2. 确保数据库有足够的测试数据
3. 失败测试会自动截图和保留trace，便于分析
4. 部分测试有容错处理，数据缺失时会跳过而非失败

### 7.3 注意事项
- 测试中尽量避免实际提交数据（如发货、提现），大部分测试只做表单验证
- 商户账号不要在其他地方同时登录，避免会话冲突
- 测试运行期间保持网络稳定
