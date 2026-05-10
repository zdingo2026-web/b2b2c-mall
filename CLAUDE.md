# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
mvn package -DskipTests                                        # Full build
mvn package -DskipTests -pl mall-common,mall-model,mall-dao,mall-service,mall-admin -am  # Specific modules
java -Xmx256m -jar mall-api/target/mall-api-1.0.0-SNAPSHOT.jar --server.port=8080   # C-End API
java -Xmx256m -jar mall-admin/target/mall-admin-1.0.0-SNAPSHOT.jar --server.port=8082  # Admin API
cd mall-admin-web && npm run dev          # Admin frontend (dev)
cd mall-pc && npm run dev                # PC storefront (Nuxt3 SSR)
cd mall-mobile && npm run dev:h5         # Mobile (H5)
cd mall-mobile && npm run dev:mp-weixin  # Mobile (WeChat mini-program)
docker-compose up -d                     # All infra + backend
```

No Maven wrapper (`mvnw`). No XML mapper files — all queries use MyBatis-Plus `BaseMapper` with `LambdaQueryWrapper`.

## Tech Stack

Java 8, Spring Boot 2.7.18, MyBatis-Plus 3.5.5, MySQL 5.7, Redis, RabbitMQ, MinIO. B2B2C mall platform.

## Module Dependency Chain

```
mall-common → mall-model → mall-dao → mall-service → {mall-admin, mall-api, mall-job}
```

Module details, base classes, and multi-tenant isolation flow → [architecture.md](.claude/reference/architecture.md)

## Three User Types

| Type | Code | Role | Tenant Filter |
|------|------|------|---------------|
| Platform admin | 1 | `ROLE_PLATFORM` | No (sees all data) |
| Merchant admin | 2 | `ROLE_MERCHANT` | Yes (`WHERE tenant_id = ?`) |
| Member/consumer | 3 | `ROLE_MEMBER` | No tenant ID |

## Key Conventions

- MySQL Connector/J 5.1.x requires `characterEncoding=UTF-8` (not `utf8mb4`) in JDBC URL
- Admin login: `POST /api/v1/platform/auth/login` with form params (`@RequestParam`), not JSON body
- Admin default credentials: username `admin`, password `123456`
- MinIO config uses `@ConfigurationProperties(prefix = "minio")` with explicit setters (no `@Data` on config class)
- `PageResult.list` field name must not be renamed — frontend reads `data.list` after Axios interceptor unwraps `R<T>` envelope

## Reference Docs

- [Architecture & Multi-Tenant](.claude/reference/architecture.md) — module details, base classes, tenant isolation, controller organization, interceptor chain
- [Infrastructure & Security](.claude/reference/infrastructure.md) — database management, security config, Docker services
