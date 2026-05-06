# 商家列表自动化测试使用说明

## 一、快速开始
### 1. 环境准备
```bash
# 安装依赖
cd e2e
npm install

# 安装浏览器
npx playwright install chromium
```

### 2. 运行测试
```bash
# 运行所有测试
npm run test:merchant

# 运行单个测试
npx playwright test tests/merchant-list.spec.ts -g "步骤1"
```

### 3. 查看测试报告
```bash
npx playwright show-report
```

## 二、完整验证流程（必做）
### 代码修改后验证步骤：
1. **停止旧服务**
```bash
# 找到并停止运行中的mall-admin进程
jps | findstr "mall-admin"
wmic process where "name='java.exe' and commandline like '%mall-admin%'" delete
```

2. **重新打包**
```bash
mvn package -DskipTests -pl mall-admin -am
```

3. **启动服务**
```bash
java -Xmx256m -jar mall-admin/target/mall-admin-1.0.0-SNAPSHOT.jar --server.port=8082
```

4. **等待启动**
等待30-60秒，直到服务完全启动

5. **验证接口**
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/v1/platform/auth/login -d "username=admin&password=123456"
# 期望返回200
```

6. **运行自动化测试**
```bash
cd e2e
npm run test:merchant
```

7. **验证结果**
所有15个测试用例全部通过即为验证完成。

---

## 三、测试覆盖范围
✅ **页面导航验证**：登录 → 进入商家列表页面
✅ **页面元素验证**：标题、筛选器、表格、分页等元素
✅ **数据验证**：表格数据加载、分页功能
✅ **状态筛选**：待审核、已通过、已拒绝、已禁用
✅ **搜索功能**：按商家名称、联系人、电话号码搜索
✅ **查看详情**：点击查看按钮弹出详情对话框
✅ **评分功能**：商家评分、品牌认证设置
✅ **禁用/启用**：商家状态切换
✅ **分页功能**：翻页、跳转页码

## 三、问题修复记录
### 已修复的问题：
1. **✅ 后端缺失评分接口**：添加了 `PUT /v1/platform/tenant/{id}/score` 接口
2. **✅ TypeScript接口不完整**：补充了Tenant接口的缺失字段
3. **✅ 移除any类型转换**：修复了前端代码中的类型错误
4. **✅ 添加搜索功能**：支持按商家名称、联系人、电话模糊搜索

## 四、配置说明
### playwright.config.ts 配置项：
```typescript
export default defineConfig({
  testDir: './tests',
  timeout: 30000, // 单测试超时时间
  retries: 0, // 失败重试次数
  use: {
    baseURL: 'http://localhost:5174', // 测试环境地址
    headless: true, // 无头模式
    screenshot: 'on', // 自动截图
    video: 'retain-on-failure', // 失败时保留视频
    trace: 'retain-on-failure', // 失败时保留追踪日志
  }
})
```

## 五、自定义扩展
### 添加新测试用例：
在 `tests/` 目录下创建 `.spec.ts` 文件，参考 `merchant-list.spec.ts` 格式编写。

### 测试工具函数：
```typescript
// 登录函数
loginAsPlatformAdmin(page: Page)

// 导航到商家列表
navigateToMerchantList(page: Page)
```

## 六、CI/CD集成
可以将此测试集成到CI流水线中，每次代码提交时自动运行：
```yaml
# GitHub Actions 示例
- run: cd e2e && npm install
- run: npx playwright install --with-deps
- run: npx playwright test
- uses: actions/upload-artifact@v3
  if: always()
  with:
    name: playwright-report
    path: playwright-report/
```
