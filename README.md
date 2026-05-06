
# Mall - B2B2C电商平台

一个功能完整的B2B2C电商平台，基于Spring Boot + Vue + UniApp技术栈构建。

## 技术栈

### 后端
- Java 8
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.5
- MySQL 5.7
- Redis 6
- RabbitMQ 3
- MinIO

### 前端
- Admin后台: Vue 3 + Vite + TypeScript + Tailwind CSS
- PC商城: Nuxt 3 + TypeScript
- 移动端: UniApp + Vue 3 + TypeScript

### 测试
- Playwright E2E测试

## 项目结构

```
mall/
├── mall-common/      # 公共模块：工具类、基础类、安全配置
├── mall-model/       # 实体模型：Entity、DTO、VO
├── mall-dao/         # 数据访问：Mapper、MyBatis-Plus配置
├── mall-service/     # 业务逻辑：服务层
├── mall-admin/       # 管理后台API：平台+商户端（端口8082）
├── mall-api/         # 消费者API：C端接口（端口8080/8081）
├── mall-job/         # 定时任务：Quartz调度
├── mall-admin-web/   # 管理后台前端
├── mall-pc/          # PC商城前端
├── mall-mobile/      # 移动端前端
├── e2e/              # E2E测试
├── docs/             # 文档
└── scripts/          # 脚本
```

## 功能特性

### 平台管理
- 商户审核与管理
- 商品审核
- 订单管理
- 会员管理
- 公告管理
- 数据统计

### 商户管理
- 店铺管理
- 商品管理
- 订单处理
- 退款管理
- 财务管理
- 数据看板

### 消费者端
- 商品浏览与搜索
- 购物车
- 订单管理
- 个人中心
- 收货地址
- 收藏与足迹

## 快速开始

### 环境要求
- JDK 8+
- Node.js 16+
- MySQL 5.7
- Redis 6
- RabbitMQ 3
- MinIO

### Docker一键启动

```bash
# 启动所有基础设施和后端服务
docker-compose up -d
```

### 后端构建

```bash
# 完整构建（跳过测试）
mvn package -DskipTests

# 构建指定模块
mvn package -DskipTests -pl mall-common,mall-model,mall-dao,mall-service,mall-admin -am
```

### 运行后端服务

```bash
# API服务（C端）
java -Xmx256m -jar mall-api/target/mall-api-1.0.0-SNAPSHOT.jar --server.port=8080

# Admin服务（管理后台）
java -Xmx256m -jar mall-admin/target/mall-admin-1.0.0-SNAPSHOT.jar --server.port=8082
```

### 前端开发

```bash
# 管理后台
cd mall-admin-web && npm run dev

# PC商城
cd mall-pc && npm run dev

# 移动端H5
cd mall-mobile && npm run dev:h5
```

## 默认账号

- 平台管理员: admin / 123456

## 数据库

数据库初始化脚本位于 `docs/architecture/init_fixed.sql`

## 许可证

本项目采用 Apache License 2.0 许可证。任何人都可以自由使用、修改、分发和商用，不受任何约束。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题，请提交 Issue。

