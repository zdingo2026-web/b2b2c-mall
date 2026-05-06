# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Full build (skip tests)
mvn package -DskipTests

# Build specific modules only
mvn package -DskipTests -pl mall-common,mall-model,mall-dao,mall-service,mall-admin -am

# Run backend services
java -Xmx256m -jar mall-api/target/mall-api-1.0.0-SNAPSHOT.jar --server.port=8080
java -Xmx256m -jar mall-admin/target/mall-admin-1.0.0-SNAPSHOT.jar --server.port=8082

# Frontend (admin)
cd mall-admin-web && npm run dev    # dev server
cd mall-admin-web && npm run build  # production build

# Frontend (PC storefront, Nuxt3 SSR)
cd mall-pc && npm run dev
cd mall-pc && npm run build

# Frontend (mobile, UniApp)
cd mall-mobile && npm run dev:h5
cd mall-mobile && npm run dev:mp-weixin

# Docker (all infra + backend)
docker-compose up -d
```

There is no Maven wrapper (`mvnw`). No XML mapper files exist — all queries use MyBatis-Plus `BaseMapper` with `LambdaQueryWrapper` in Java.

## Architecture

B2B2C mall platform. Java 8, Spring Boot 2.7.18, MyBatis-Plus 3.5.5, MySQL 5.7, Redis, RabbitMQ, MinIO.

### Module Dependency Chain

```
mall-common → mall-model → mall-dao → mall-service → {mall-admin, mall-api, mall-job}
```

| Module | Role | Port |
|--------|------|------|
| `mall-common` | Utilities, base classes, Security+JWT config, constants, exceptions | - |
| `mall-model` | Entities, DTOs, VOs | - |
| `mall-dao` | Mappers, MyBatis-Plus config, tenant interceptor | - |
| `mall-service` | Business logic (auth, products, orders, payments, content, MQ) | - |
| `mall-admin` | Admin backend (Platform + Merchant APIs) | 8082 |
| `mall-api` | C-End consumer API | 8080/8081 |
| `mall-job` | Scheduled tasks (Quartz) | 8082 |

### Key Base Classes

- **`BaseEntity`** (`mall-common`): `id` (snowflake ASSIGN_ID), `createTime`, `updateTime`, `deleted` (logical delete, `@TableLogic`)
- **`TenantEntity`** extends `BaseEntity`: adds `tenantId` — all B2B2C business tables use this
- **`R<T>`**: Unified API response with `code`, `msg`, `data` fields
- **`PageResult<T>`**: Paginated response with `list`, `total`, `page`, `limit` fields

### Three User Types

| Type | Code | Role | Tenant Filter |
|------|------|------|---------------|
| Platform admin | 1 | `ROLE_PLATFORM` | No (sees all data) |
| Merchant admin | 2 | `ROLE_MERCHANT` | Yes (`WHERE tenant_id = ?`) |
| Member/consumer | 3 | `ROLE_MEMBER` | No tenant ID |

### Multi-Tenant Isolation

Implemented at DAO layer via MyBatis-Plus `TenantLineInnerInterceptor`:

1. JWT token contains `userId`, `userType`, `tenantId`
2. `JwtAuthenticationFilter` sets `TenantContext` (ThreadLocal) + `UserContext` + Spring Security context
3. `MallTenantLineHandler` reads `TenantContext.getTenantId()`:
   - If null or 0 (platform), `ignoreTable()` returns true — no filter injected
   - Otherwise, injects `WHERE tenant_id = ?` into all SQL
4. Tables without `tenant_id` are exempted via `SYSTEM_TABLES` set in `MallTenantLineHandler`
5. `@TenantIgnore` annotation on mapper methods/classes skips tenant filtering entirely
6. Platform data uses `tenantId = 0` (constant: `CommonConstant.PLATFORM_TENANT_ID`)

### Admin Controller Organization

- `com.mall.admin.controller.platform.*` — Platform admin endpoints (`/api/v1/platform/**`), role: `ROLE_PLATFORM`
- `com.mall.admin.controller.merchant.*` — Merchant admin endpoints (`/api/v1/merchant/**`), role: `ROLE_MERCHANT`
- `com.mall.api.controller.*` — C-End consumer endpoints (`/api/v1/member/**`)

### Interceptor Chain (MybatisPlusConfig)

Tenant → Optimistic Lock → Pagination (max 100 per page)

### Database

- No auto-DDL — schema managed via SQL scripts in `docs/architecture/`
- `init_fixed.sql` is the canonical init script (mounted into Docker MySQL)
- Migration scripts: `v2_migration.sql`, `v3_migration.sql`, etc.
- When entity fields are added, corresponding ALTER TABLE must be applied to the database

### Security

- JWT stateless auth, CSRF disabled, sessions STATELESS
- Password: BCrypt via `at.favre.lib:bcrypt` (wrapped in `PasswordUtil`)
- Token blacklist: logout stores access token in Redis with TTL as expiry
- `@PreAuthorize("hasRole('MERCHANT')")` annotations on controller methods
- Public endpoints: auth URLs, Knife4j/Swagger docs, Druid monitoring, public product GET endpoints

### Docker Services

MySQL 5.7 (port 3306), Redis 6 (6379), RabbitMQ 3 (5672/15672, user: mall/mall123), MinIO (9000/9001, default: minioadmin/minioadmin), backend (8080), admin-web nginx (5174), mobile-web nginx (3002)

## Key Conventions

- MySQL Connector/J 5.1.x requires `characterEncoding=UTF-8` (not `utf8mb4`) in JDBC URL
- Admin login: `POST /api/v1/platform/auth/login` with form params (`@RequestParam`), not JSON body
- Admin default credentials: username `admin`, password `123456`
- MinIO config uses `@ConfigurationProperties(prefix = "minio")` with explicit setters (no `@Data` on config class)
- `PageResult.list` field name must not be renamed — frontend reads `data.list` after Axios interceptor unwraps `R<T>` envelope
