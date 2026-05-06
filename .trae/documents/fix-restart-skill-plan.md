# 修复重启技能并执行前后端重启

## 问题描述

重启技能文件存在但目录结构不正确：
- ❌ 当前：`.trae/skills/restart.md`
- ✅ 预期：`.trae/skills/restart/SKILL.md`

## 修复步骤

1. 创建 `restart/` 目录
2. 将 `restart.md` 移动到 `restart/SKILL.md`
3. 删除旧的 `restart.md` 文件
4. 调用 `restart` 技能重启前后端服务
5. 继续执行商户管理后台 E2E 测试

## 技能内容

该技能会：
- 停止 8080、8082、5174、3002 端口上的服务
- 构建后端（mall-common, mall-model, mall-dao, mall-service, mall-api, mall-admin）
- 启动 mall-api (8080) 和 mall-admin (8082) 后端服务
- 启动 mall-admin-web (5174) 前端服务
- 验证服务状态
