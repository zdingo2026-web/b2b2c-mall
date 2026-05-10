# Architecture Reference

## Module Details

| Module | Role | Port |
|--------|------|------|
| `mall-common` | Utilities, base classes, Security+JWT config, constants, exceptions | - |
| `mall-model` | Entities, DTOs, VOs | - |
| `mall-dao` | Mappers, MyBatis-Plus config, tenant interceptor | - |
| `mall-service` | Business logic (auth, products, orders, payments, content, MQ) | - |
| `mall-admin` | Admin backend (Platform + Merchant APIs) | 8082 |
| `mall-api` | C-End consumer API | 8080/8081 |
| `mall-job` | Scheduled tasks (Quartz) | 8082 |

## Key Base Classes

- **`BaseEntity`** (`mall-common`): `id` (snowflake ASSIGN_ID), `createTime`, `updateTime`, `deleted` (logical delete, `@TableLogic`)
- **`TenantEntity`** extends `BaseEntity`: adds `tenantId` — all B2B2C business tables use this
- **`R<T>`**: Unified API response with `code`, `msg`, `data` fields
- **`PageResult<T>`**: Paginated response with `list`, `total`, `page`, `limit` fields

## Multi-Tenant Isolation

Implemented at DAO layer via MyBatis-Plus `TenantLineInnerInterceptor`:

1. JWT token contains `userId`, `userType`, `tenantId`
2. `JwtAuthenticationFilter` sets `TenantContext` (ThreadLocal) + `UserContext` + Spring Security context
3. `MallTenantLineHandler` reads `TenantContext.getTenantId()`:
   - If null or 0 (platform), `ignoreTable()` returns true — no filter injected
   - Otherwise, injects `WHERE tenant_id = ?` into all SQL
4. Tables without `tenant_id` are exempted via `SYSTEM_TABLES` set in `MallTenantLineHandler`
5. `@TenantIgnore` annotation on mapper methods/classes skips tenant filtering entirely
6. Platform data uses `tenantId = 0` (constant: `CommonConstant.PLATFORM_TENANT_ID`)

## Admin Controller Organization

- `com.mall.admin.controller.platform.*` — Platform endpoints (`/api/v1/platform/**`), role: `ROLE_PLATFORM`
- `com.mall.admin.controller.merchant.*` — Merchant endpoints (`/api/v1/merchant/**`), role: `ROLE_MERCHANT`
- `com.mall.api.controller.*` — C-End consumer endpoints (`/api/v1/member/**`)

## Interceptor Chain (MybatisPlusConfig)

Tenant → Optimistic Lock → Pagination (max 100 per page)
