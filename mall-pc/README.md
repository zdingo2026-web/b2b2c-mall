# Mall PC - B2B2C商城PC端

## 技术栈

- **框架**: Nuxt3 (Vue3 + SSR)
- **状态管理**: Pinia
- **样式**: TailwindCSS
- **包管理**: npm

## 启动方式

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 环境变量

创建 `.env` 文件：

```env
# API地址
NUXT_PUBLIC_API_BASE=http://localhost:8080/api
```

## 项目结构

```
mall-pc/
├── app.vue                    # 根组件
├── nuxt.config.ts             # Nuxt配置
├── tailwind.config.ts         # TailwindCSS配置
├── assets/css/tailwind.css    # 全局样式
├── components/                # 公共组件
├── composables/
│   └── useRequest.ts          # API请求封装
├── layouts/
│   └── default.vue            # 默认布局(Header+Footer)
├── pages/
│   ├── index.vue              # 首页
│   ├── category/              # 分类页
│   ├── product/               # 商品页(列表+详情)
│   ├── cart/                  # 购物车
│   ├── order/                 # 订单(确认/列表/详情)
│   └── member/                # 会员中心
├── stores/
│   ├── user.ts                # 用户Store
│   └── cart.ts                # 购物车Store
└── public/                    # 静态资源
```

## API请求说明

使用 `composables/useRequest.ts` 封装的请求方法：

```ts
const { get, post, put, delete: del, patch } = useRequest()

// GET请求
const data = await get('/product/list', { page: 1 })

// POST请求
const result = await post('/member/cart/add', { productId: 1, skuId: 1, quantity: 2 })
```

- 自动携带 Token
- 401 自动刷新 Token
- 刷新失败自动跳转登录页
