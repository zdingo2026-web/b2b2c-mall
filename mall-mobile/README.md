# Mall Mobile - B2B2C商城移动端

## 技术栈

- **框架**: UniApp (Vue3)
- **状态管理**: Pinia
- **支持平台**: 微信小程序 / H5 / App
- **包管理**: npm

## 启动方式

```bash
# 安装依赖
npm install

# 启动微信小程序开发
npm run dev:mp-weixin

# 启动H5开发
npm run dev:h5

# 启动App开发
npm run dev:app

# 构建微信小程序
npm run build:mp-weixin

# 构建H5
npm run build:h5
```

## TabBar 页面

| Tab | 路径 | 说明 |
|-----|------|------|
| 首页 | pages/index/index | Banner轮播、分类导航、推荐商品 |
| 分类 | pages/category/index | 左侧分类树+右侧子分类/商品 |
| 购物车 | pages/cart/index | 商品列表、全选、结算 |
| 我的 | pages/mine/index | 用户信息、订单入口、功能菜单 |

## 项目结构

```
mall-mobile/
├── App.vue                     # 应用入口
├── main.ts                     # 主入口文件
├── manifest.json               # 应用配置(appid/平台配置)
├── pages.json                  # 页面路由与TabBar配置
├── vite.config.ts              # Vite配置
├── pages/
│   ├── index/                  # 首页
│   ├── category/               # 分类页
│   ├── cart/                   # 购物车
│   ├── mine/                   # 我的
│   ├── product/                # 商品详情
│   ├── search/                 # 搜索页
│   ├── order/                  # 订单(列表/详情/确认)
│   └── login/                  # 登录(手机号/微信)
├── stores/
│   ├── user.ts                 # 用户Store
│   └── cart.ts                 # 购物车Store
├── utils/
│   └── request.ts              # 请求封装(uni.request)
├── components/                 # 公共组件
└── static/                     # 静态资源
    └── tab/                    # TabBar图标
```

## API请求说明

使用 `utils/request.ts` 封装的请求方法：

```ts
import { http } from '@/utils/request'

// GET请求
const data = await http.get('/member/product/list', { page: 1 })

// POST请求
const result = await http.post('/member/cart/add', { productId: 1, skuId: 1, quantity: 2 })
```

- 自动携带 Token (uni.getStorageSync)
- 401 自动刷新 Token
- 刷新失败自动跳转登录页
- 统一错误提示 (uni.showToast)

## 注意事项

- TabBar图标文件位于 `static/tab/` 目录，需替换为实际图标
- 微信小程序需在 `manifest.json` 中配置合法 appid
- 条件编译: `<!-- #ifdef MP-WEIXIN -->` 用于微信小程序专属代码
