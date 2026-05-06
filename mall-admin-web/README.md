# Mall Admin Web - B2B2C商城管理后台

## 技术栈

- **框架**: Vue3 + Vite
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **样式**: TailwindCSS + SCSS
- **HTTP**: Axios
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

## 双端模式

管理后台同时支持 **平台管理端** 和 **商户管理端**：

- 平台管理员登录后进入 `/platform/*` 路由
- 商户管理员登录后进入 `/merchant/*` 路由
- 菜单和权限根据角色动态生成

## 项目结构

```
mall-admin-web/
├── index.html
├── vite.config.ts                # Vite配置(代理/别名)
├── tailwind.config.ts            # TailwindCSS配置
├── src/
│   ├── main.ts                   # 入口文件
│   ├── App.vue                   # 根组件
│   ├── assets/styles/            # 全局样式
│   ├── components/               # 公共组件
│   ├── layouts/
│   │   └── AdminLayout.vue       # 管理后台布局(侧边栏+头部+内容)
│   ├── router/
│   │   └── index.ts              # 路由配置(平台/商户双端)
│   ├── stores/
│   │   ├── user.ts               # 用户Store
│   │   └── permission.ts         # 权限Store(菜单生成)
│   ├── utils/
│   │   └── request.ts            # Axios请求封装
│   └── views/
│       ├── login/                # 登录页
│       ├── platform/             # 平台管理端页面
│       │   ├── dashboard/        # 工作台
│       │   ├── merchant/         # 商家管理
│       │   ├── product/          # 商品管理
│       │   ├── order/            # 订单管理
│       │   ├── member/           # 会员管理
│       │   └── system/           # 系统设置
│       └── merchant/             # 商户管理端页面
│           ├── dashboard/        # 工作台
│           ├── product/          # 商品管理
│           ├── order/            # 订单管理
│           ├── shop/             # 店铺设置
│           └── finance/          # 资金管理
```

## API请求说明

使用 `utils/request.ts` 封装的请求方法：

```ts
import { request } from '@/utils/request'

// GET请求
const data = await request.get('/admin/product/list', { page: 1 })

// POST请求
const result = await request.post('/admin/auth/login', { username: 'admin', password: '123456' })
```

- 自动携带 Token
- 401 自动刷新 Token
- 刷新失败自动跳转登录页
- 统一错误提示 (Element Plus ElMessage)
