# Infrastructure Reference

## Database

- No auto-DDL — schema managed via SQL scripts in `docs/architecture/`
- `init_fixed.sql` is the canonical init script (mounted into Docker MySQL)
- Migration scripts: `v2_migration.sql`, `v3_migration.sql`, etc.
- When entity fields are added, corresponding ALTER TABLE must be applied to the database

## Security

- JWT stateless auth, CSRF disabled, sessions STATELESS
- Password: BCrypt via `at.favre.lib:bcrypt` (wrapped in `PasswordUtil`)
- Token blacklist: logout stores access token in Redis with TTL as expiry
- `@PreAuthorize("hasRole('MERCHANT')")` annotations on controller methods
- Public endpoints: auth URLs, Knife4j/Swagger docs, Druid monitoring, public product GET endpoints

## Docker Services

| Service | Port | Credentials |
|---------|------|-------------|
| MySQL 5.7 | 3306 | - |
| Redis 6 | 6379 | - |
| RabbitMQ 3 | 5672 / 15672 | mall / mall123 |
| MinIO | 9000 / 9001 | minioadmin / minioadmin |
| Backend (mall-api) | 8080 | - |
| Admin-web nginx | 5174 | - |
| Mobile-web nginx | 3002 | - |
