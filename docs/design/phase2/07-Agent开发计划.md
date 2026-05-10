# 二期通用功能 — Agent开发计划

> 版本：v1.0 | 日期：2026-05-10 | 状态：待执行

---

## Context

二期6大模块（营销/积分/装修/分销/商家/会员）设计文档全部完成，现需制定Agent团队开发计划。工作量：~222个Java新类、15个修改类、40新表、132接口、59个前端页面。采用按层推进、全栈覆盖、最多5并行Agent。

---

## 一、开发阶段总览

```
Phase 0: 基础设施 (SQL迁移 + 枚举/常量 + Entity扩展)
    ↓
Phase 1: 数据层 (40 Entity + 40 Mapper + SYSTEM_TABLES扩展)
    ↓
Phase 2: 传输层 (25 DTO + 55 VO)
    ↓
Phase 3: 业务层 (33 Service + MQ + Redis Lua + 定时任务)
    ↓
Phase 4: 控制层 (17 Controller, 132接口)
    ↓
Phase 5: 前端 (25平台页面 + 16商户页面 + 18移动端页面)
    ↓
Phase 6: 集成验证 (编译 + 启动 + 冒烟测试)
```

## 二、Agent角色清单

| Agent名 | 职责 | 产出物 | 读取文档 |
|---------|------|--------|---------|
| **sql-writer** | 编写V7迁移SQL脚本 | `v7_migration.sql` | 05-表结构设计.md |
| **entity-writer** | 编写Entity类 | `mall-model/entity/**/*.java` | 05-表结构设计.md |
| **mapper-writer** | 编写Mapper类+扩展TenantHandler | `mall-dao/mapper/**/*.java` | 05-表结构设计.md, entity产出 |
| **dto-vo-writer** | 编写DTO/VO类 | `mall-model/dto/**/*.java`, `mall-model/vo/**/*.java` | 06-接口设计.md |
| **service-writer** | 编写Service+MQ+定时任务 | `mall-service/**/*.java` | 01-技术方案.md, 06-接口设计.md |
| **controller-writer** | 编写Controller | `mall-admin/**/*.java`, `mall-api/**/*.java` | 06-接口设计.md |
| **frontend-platform** | 平台管理端前端页面 | `mall-admin-web/src/views/platform/**` | 02-页面设计.md, 06-接口设计.md, Calicat |
| **frontend-merchant** | 商户管理端前端页面 | `mall-admin-web/src/views/merchant/**` | 02-页面设计.md, 06-接口设计.md, Calicat |
| **frontend-mobile** | 移动端页面 | `mall-mobile/src/pages/**` | 02-页面设计.md, 06-接口设计.md, Calicat |
| **integrator** | 集成验证+修复 | 编译报告, 冒烟测试结果 | 全部产出物 |

## 三、Agent进度状态定义

| 状态 | 含义 | 可转换到 |
|------|------|---------|
| `NOT_STARTED` | 未开始 | → PENDING |
| `PENDING` | 待启动（前置条件检查通过） | → IN_PROGRESS, → BLOCKED |
| `IN_PROGRESS` | 进行中 | → PAUSED, → ERROR, → COMPLETED |
| `PAUSED` | 暂停（人工暂停） | → IN_PROGRESS |
| `BLOCKED` | 阻塞（缺前置材料） | → PENDING（材料补齐后） |
| `ERROR` | 出错（编译/运行失败） | → IN_PROGRESS（修复后重试） |
| `COMPLETED` | 已完成 | — |

**BLOCKED处理流程**：
1. Agent启动前检查前置文档/代码是否齐全
2. 不齐全 → 写入进度表备注（缺什么），状态设为BLOCKED
3. 前置Agent完成后，手动或自动将BLOCKED改为PENDING
4. Agent重新启动

## 四、Phase详细计划

### Phase 0: 基础设施

| Agent | 任务 | 前置依赖 | 产出物 | 预计文件数 |
|-------|------|---------|--------|-----------|
| sql-writer | 编写v7_migration.sql: 40张新建表CREATE TABLE + 5张ALTER TABLE扩展 + 种子数据 | 05-表结构设计.md | `docs/architecture/v7_migration.sql` | 1 |
| sql-writer | 扩展枚举类: OrderTypeEnum新增SECKILL/GROUP_BUY/GROUPON/POINTS, 新增CouponTypeEnum/CouponStatusEnum/GroupBuyStatusEnum/CommissionStatusEnum/PointsBizTypeEnum/PromotionTypeEnum | 01-技术方案.md | `mall-common/enums/*.java` | 7 |
| sql-writer | 扩展RedisKeyConstant: 新增秒杀/拼团/积分/分销/装修相关key | 01-技术方案.md(3.9节) | `mall-common/constant/RedisKeyConstant.java` | 1 |
| sql-writer | 修改5个已有Entity: MemberCoupon/OrderMain/Tenant/Member/MemberCollect 扩展字段 | 05-表结构设计.md | `mall-model/entity/*.java` | 5 |

**并行度**: 1个Agent（sql-writer），顺序执行4个任务

### Phase 1: 数据层

| Agent | 任务 | 前置依赖 | 产出物 | 预计文件数 |
|-------|------|---------|--------|-----------|
| entity-writer-1 | 营销12 Entity: CouponTemplate, SeckillActivity, SeckillSku, GroupBuyActivity, GroupBuyRecord, GroupBuyMember, GrouponActivity, GrouponProduct, DiscountActivity, DiscountProduct, NewcomerPack, FirstOrderConfig | Phase 0完成 | `mall-model/entity/promotion/*.java` | 12 |
| entity-writer-2 | 积分7 Entity + 装修4 Entity: PointsRule, PointsConsumeRule, PointsAccount, PointsDetail, PointsProductCategory, PointsProduct, PointsOrder, DecoPage, DecoTemplate, DecoAlbum, DecoImage | Phase 0完成 | `mall-model/entity/points/*.java`, `mall-model/entity/decoration/*.java` | 11 |
| entity-writer-3 | 分销6 Entity + 商家5 Entity + 会员6 Entity: DistributionConfig, Distributor, DistributionProduct, DistributionRelation, CommissionRecord, WithdrawRecord, TenantLevel, CategoryCommission, TenantCommissionSettlement, TenantFreezeRecord, TenantSettleConfig, RedPacketBatch, MemberRedPacket, MemberLevel, MemberCheckinRecord, MemberRealnameAuth, MemberPaypwdReset | Phase 0完成 | `mall-model/entity/distribution/*.java`, `mall-model/entity/tenant/*.java`, `mall-model/entity/member/*.java` | 17 |
| mapper-writer | 40 Mapper接口 + MallTenantLineHandler扩展SYSTEM_TABLES | Entity全部完成 | `mall-dao/mapper/**/*.java` | 41 |

**并行度**: 3个entity-writer并行(Phase1前半段) → 1个mapper-writer(Phase1后半段)

### Phase 2: 传输层

| Agent | 任务 | 前置依赖 | 产出物 | 预计文件数 |
|-------|------|---------|--------|-----------|
| dto-writer-1 | 营销9 DTO + 积分4 DTO + 装修3 DTO | Phase 1完成, 06-接口设计.md | `mall-model/dto/promotion/*.java`, `mall-model/dto/points/*.java`, `mall-model/dto/decoration/*.java` | 16 |
| dto-writer-2 | 分销4 DTO + 商家4 DTO + 会员1 DTO | Phase 1完成, 06-接口设计.md | `mall-model/dto/distribution/*.java`, `mall-model/dto/tenant/*.java`, `mall-model/dto/member/*.java` | 9 |
| vo-writer-1 | 营销17 VO + 积分10 VO | Phase 1完成, 06-接口设计.md | `mall-model/vo/promotion/*.java`, `mall-model/vo/points/*.java` | 27 |
| vo-writer-2 | 分销10 VO + 商家10 VO + 会员8 VO + 订单1 VO | Phase 1完成, 06-接口设计.md | `mall-model/vo/distribution/*.java`, `mall-model/vo/tenant/*.java`, `mall-model/vo/member/*.java` | 29 |

**并行度**: 4个Agent并行（2 DTO + 2 VO）

### Phase 3: 业务层

| Agent | 任务 | 前置依赖 | 产出物 | 预计文件数 |
|-------|------|---------|--------|-----------|
| service-writer-1 | 营销核心: PromotionStrategy接口 + 7个策略实现 + PromotionEngine + CouponStackValidator + CouponService + SeckillService(含Redis Lua) | Phase 2完成, 01-技术方案.md | `mall-service/promotion/*.java` | ~12 |
| service-writer-2 | 拼团/团购/折扣: GroupBuyService + GrouponService + DiscountService + NewcomerPackService + FirstOrderService | Phase 2完成, 01-技术方案.md | `mall-service/promotion/*.java` | ~5 |
| service-writer-3 | 积分+装修: PointsService + PointsProductService + PointsOrderService + DecoPageService + DecoTemplateService + DecoImageService + DecoRenderer | Phase 2完成, 01-技术方案.md | `mall-service/points/*.java`, `mall-service/decoration/*.java` | ~7 |
| service-writer-4 | 分销+商家+会员: DistributionService + CommissionService + DistributorService + WithdrawService + TenantLevelService + CategoryCommissionService + RedPacketService + MemberLevelService + CheckinService + RealnameService | Phase 2完成, 01-技术方案.md | `mall-service/distribution/*.java`, `mall-service/tenant/*.java`, `mall-service/member/*.java` | ~10 |
| mq-writer | MQ生产者/消费者 + RabbitMQConfig扩展 + 定时任务 + Redis Lua脚本 | Phase 2完成, 01-技术方案.md | `mall-service/mq/**/*.java`, `mall-job/handler/*.java` | ~8 |

**并行度**: 5个Agent并行

### Phase 4: 控制层

| Agent | 任务 | 前置依赖 | 产出物 | 预计文件数 |
|-------|------|---------|--------|-----------|
| ctrl-platform | 平台端7 Controller: PlatformCouponController + PlatformSeckillController + PlatformPromotionController + PlatformPointsController + PlatformDistributionController + PlatformTenantEnhanceController + PlatformMemberEnhanceController | Phase 3完成, 06-接口设计.md | `mall-admin/controller/platform/*.java` | 7 |
| ctrl-merchant | 商户端5 Controller: MerchantCouponController + MerchantGroupBuyController + MerchantDecoController + MerchantDistributionController + MerchantDashboardController | Phase 3完成, 06-接口设计.md | `mall-admin/controller/merchant/*.java` | 5 |
| ctrl-mobile | 移动端6 Controller: MemberPromotionController + MemberSeckillController + MemberGroupBuyController + MemberPointsController + MemberDistributionController + MemberShopController | Phase 3完成, 06-接口设计.md | `mall-api/controller/member/*.java` | 6 |

**并行度**: 3个Agent并行

### Phase 5: 前端

| Agent | 任务 | 前置依赖 | 产出物 | 预计页面数 |
|-------|------|---------|--------|-----------|
| frontend-platform | 平台管理端25页面: API接口定义 + 路由配置 + 页面组件 | Phase 4完成, 02-页面设计.md, Calicat设计稿 | `mall-admin-web/src/views/platform/**/*.vue` | 25 |
| frontend-merchant | 商户管理端16页面: API接口定义 + 路由配置 + 页面组件(含装修编辑器) | Phase 4完成, 02-页面设计.md, Calicat设计稿 | `mall-admin-web/src/views/merchant/**/*.vue` | 16 |
| frontend-mobile | 移动端18页面: API接口定义 + 页面路由 + 组件(H5+小程序) | Phase 4完成, 02-页面设计.md, Calicat设计稿 | `mall-mobile/src/pages/**/*.vue` | 18 |

**并行度**: 3个Agent并行

### Phase 6: 集成验证

| Agent | 任务 | 前置依赖 | 产出物 |
|-------|------|---------|--------|
| integrator | 1. mvn compile全量编译 2. 启动应用验证 3. 冒烟测试关键接口 4. 修复编译/启动错误 | Phase 4完成(前端可并行) | 编译报告, 冒烟测试报告 |

## 五、Agent进度总表

| Agent | Phase | 状态 | 前置依赖 | 产出物路径 | 备注 |
|-------|-------|------|---------|-----------|------|
| sql-writer | 0 | NOT_STARTED | 05-表结构设计.md, 01-技术方案.md | `docs/architecture/v7_migration.sql`, `mall-common/enums/`, `mall-common/constant/`, `mall-model/entity/` | - |
| entity-writer-1 | 1 | NOT_STARTED | Phase 0完成 | `mall-model/entity/promotion/` | - |
| entity-writer-2 | 1 | NOT_STARTED | Phase 0完成 | `mall-model/entity/points/`, `mall-model/entity/decoration/` | - |
| entity-writer-3 | 1 | NOT_STARTED | Phase 0完成 | `mall-model/entity/distribution/`, `mall-model/entity/tenant/`, `mall-model/entity/member/` | - |
| mapper-writer | 1 | NOT_STARTED | Entity全部完成 | `mall-dao/mapper/`, `mall-dao/tenant/MallTenantLineHandler.java` | - |
| dto-writer-1 | 2 | NOT_STARTED | Phase 1完成, 06-接口设计.md | `mall-model/dto/promotion/`, `mall-model/dto/points/`, `mall-model/dto/decoration/` | - |
| dto-writer-2 | 2 | NOT_STARTED | Phase 1完成, 06-接口设计.md | `mall-model/dto/distribution/`, `mall-model/dto/tenant/`, `mall-model/dto/member/` | - |
| vo-writer-1 | 2 | NOT_STARTED | Phase 1完成, 06-接口设计.md | `mall-model/vo/promotion/`, `mall-model/vo/points/` | - |
| vo-writer-2 | 2 | NOT_STARTED | Phase 1完成, 06-接口设计.md | `mall-model/vo/distribution/`, `mall-model/vo/tenant/`, `mall-model/vo/member/` | - |
| service-writer-1 | 3 | NOT_STARTED | Phase 2完成, 01-技术方案.md | `mall-service/src/main/java/com/mall/service/promotion/` | 营销核心(策略+引擎+秒杀) |
| service-writer-2 | 3 | NOT_STARTED | Phase 2完成, 01-技术方案.md | `mall-service/src/main/java/com/mall/service/promotion/` | 拼团/团购/折扣/礼包 |
| service-writer-3 | 3 | NOT_STARTED | Phase 2完成, 01-技术方案.md | `mall-service/src/main/java/com/mall/service/points/`, `decoration/` | 积分+装修 |
| service-writer-4 | 3 | NOT_STARTED | Phase 2完成, 01-技术方案.md | `mall-service/src/main/java/com/mall/service/distribution/`, `tenant/`, `member/` | 分销+商家+会员 |
| mq-writer | 3 | NOT_STARTED | Phase 2完成, 01-技术方案.md | `mall-service/src/main/java/com/mall/service/mq/`, `mall-job/` | MQ+定时任务+Lua |
| ctrl-platform | 4 | NOT_STARTED | Phase 3完成, 06-接口设计.md | `mall-admin/src/main/java/com/mall/admin/controller/platform/` | 52接口/7Controller |
| ctrl-merchant | 4 | NOT_STARTED | Phase 3完成, 06-接口设计.md | `mall-admin/src/main/java/com/mall/admin/controller/merchant/` | 38接口/5Controller |
| ctrl-mobile | 4 | NOT_STARTED | Phase 3完成, 06-接口设计.md | `mall-api/src/main/java/com/mall/api/controller/member/` | 42接口/6Controller |
| frontend-platform | 5 | NOT_STARTED | Phase 4完成, 02-页面设计.md | `mall-admin-web/src/views/platform/` | 25页面 |
| frontend-merchant | 5 | NOT_STARTED | Phase 4完成, 02-页面设计.md | `mall-admin-web/src/views/merchant/` | 16页面 |
| frontend-mobile | 5 | NOT_STARTED | Phase 4完成, 02-页面设计.md | `mall-mobile/src/pages/` | 18页面 |
| integrator | 6 | NOT_STARTED | Phase 4完成 | 编译报告, 冒烟测试报告 | - |

## 六、Agent文档交互矩阵

| Agent | 读取文档 | 写入产出 | 被谁消费 |
|-------|---------|---------|---------|
| sql-writer | 05-表结构设计.md, 01-技术方案.md | v7_migration.sql, 枚举/常量/Entity扩展 | entity-writer-*, mapper-writer |
| entity-writer-* | 05-表结构设计.md, sql-writer产出 | Entity类 | mapper-writer, dto/vo-writer |
| mapper-writer | 05-表结构设计.md, Entity类 | Mapper类, TenantHandler扩展 | service-writer-* |
| dto/vo-writer | 06-接口设计.md | DTO/VO类 | service-writer-*, controller-writer-* |
| service-writer-* | 01-技术方案.md, 06-接口设计.md, DTO/VO | Service类 | controller-writer-* |
| mq-writer | 01-技术方案.md(MQ/定时任务章节) | MQ类, Job类 | service-writer-*, integrator |
| ctrl-* | 06-接口设计.md, Service类 | Controller类 | frontend-*, integrator |
| frontend-* | 02-页面设计.md, 06-接口设计.md, Calicat设计稿 | Vue页面 | integrator |
| integrator | 全部产出物 | 编译/测试报告 | 人工 |

## 七、执行时序图

```
时间 ──────────────────────────────────────────────────────────────────────>

Phase 0:  [sql-writer]
Phase 1:  [entity-1][entity-2][entity-3]  →  [mapper-writer]
Phase 2:  [dto-1][dto-2][vo-1][vo-2]
Phase 3:  [svc-1][svc-2][svc-3][svc-4][mq]
Phase 4:  [ctrl-platform][ctrl-merchant][ctrl-mobile]
Phase 5:  [fe-platform][fe-merchant][fe-mobile]  ← 可与Phase4后半段并行
Phase 6:  [integrator]
```

## 八、BLOCKED检查规则

每个Agent启动前执行前置检查：

| Agent | 检查项 | BLOCKED条件 |
|-------|--------|------------|
| sql-writer | 05-表结构设计.md存在 | 文件不存在 → BLOCKED |
| entity-writer-* | Phase 0的枚举/常量/Entity扩展已写入 | 缺少 → BLOCKED，备注"缺Phase 0产出" |
| mapper-writer | 40个Entity文件全部存在 | 任一缺失 → BLOCKED，备注"缺Entity: xxx" |
| dto/vo-writer | Phase 1 Entity+Mapper完成 | 编译失败 → BLOCKED |
| service-writer-* | Phase 2 DTO/VO文件存在 | 缺少 → BLOCKED |
| mq-writer | Phase 2完成 | 缺少 → BLOCKED |
| ctrl-* | Phase 3 Service文件存在 | 缺少 → BLOCKED |
| frontend-* | API接口可调用(Mock或真实) | 接口不通 → BLOCKED，备注"缺API" |
| integrator | Phase 4全部Controller完成 | 缺少 → BLOCKED |

## 九、验证方案

### Phase 6 集成验证检查清单

1. **编译验证**: `mvn compile` 全量编译通过
2. **启动验证**: `mall-admin` 和 `mall-api` 能正常启动
3. **SQL验证**: v7_migration.sql 在MySQL 5.7上执行无错误
4. **接口冒烟测试**:
   - 平台端: 创建优惠券模板 → 查看列表 → 启用/禁用
   - 商户端: 创建拼团活动 → 查看列表
   - 移动端: 领取优惠券 → 查看我的优惠券
5. **租户隔离验证**: 商户A创建的券，商户B不可见
6. **前端编译**: `npm run build` 管理端和移动端均通过

## 十、执行方法

在新session中执行时，使用以下提示词引导IDE按计划执行：

### 启动提示词

```
请阅读 docs/design/phase2/07-Agent开发计划.md，按照Phase顺序执行开发计划。

当前执行阶段：Phase X

执行规则：
1. 每个Phase完成后，更新进度总表中的状态
2. 启动Agent前先检查前置依赖，缺材料则标记BLOCKED
3. 同一Phase内的Agent尽量并行启动（最多5个）
4. 每个Agent需读取对应的设计文档（05-表结构设计.md / 06-接口设计.md / 01-技术方案.md / 02-页面设计.md）
5. 产出物必须放在计划指定的路径
6. 参考一期已有代码的包结构、命名风格、基类继承方式
```

### 逐Phase执行

- **Phase 0**: `"执行Phase 0: 启动sql-writer Agent，读取05-表结构设计.md和01-技术方案.md，产出v7_migration.sql、枚举扩展、RedisKeyConstant扩展、5个Entity修改"`
- **Phase 1**: `"执行Phase 1: 先并行启动entity-writer-1/2/3三个Agent，完成后启动mapper-writer Agent"`
- **Phase 2**: `"执行Phase 2: 并行启动dto-writer-1/2和vo-writer-1/2四个Agent"`
- **Phase 3**: `"执行Phase 3: 并行启动service-writer-1/2/3/4和mq-writer五个Agent"`
- **Phase 4**: `"执行Phase 4: 并行启动ctrl-platform/ctrl-merchant/ctrl-mobile三个Agent"`
- **Phase 5**: `"执行Phase 5: 并行启动frontend-platform/frontend-merchant/frontend-mobile三个Agent"`
- **Phase 6**: `"执行Phase 6: 启动integrator Agent，执行全量编译、启动验证、冒烟测试"`

### 恢复中断

如果某个Phase中断，在新session中：

```
请阅读 docs/design/phase2/07-Agent开发计划.md，检查当前各Phase产出物完成情况，
从最后一个未完成的Phase继续执行。
```

---

*文档结束*
