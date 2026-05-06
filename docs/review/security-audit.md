# B2B2C Mall Security Audit Report

**Date**: 2026-04-30
**Scope**: Phase 1 MVP — Backend services, authentication, multi-tenant isolation, payment, file upload, SMS
**Auditor**: Automated security review

---

## Summary

| Severity | Count |
|----------|-------|
| CRITICAL | 3 |
| HIGH | 5 |
| MEDIUM | 4 |
| LOW | 3 |

Overall risk: **HIGH** — Three critical findings require immediate remediation before production deployment.

---

## CRITICAL Findings

### C-01: Hardcoded JWT Secret Key

**File**: `mall-common/.../util/JwtUtil.java:24`
**Description**: The JWT secret has a hardcoded default value:
```java
@Value("${mall.jwt.secret:mallB2B2CSecretKeyForJwtTokenGenerationMustBe32Chars!!}")
```
This default is compiled into the binary and can be extracted by anyone with access to the JAR. If the property is not explicitly configured in production, all tokens can be forged.

**Impact**: Full authentication bypass — attacker can create valid tokens for any user, including platform admin.

**Remediation**:
1. Remove the default value; fail fast on startup if `mall.jwt.secret` is not configured.
2. Enforce minimum secret length (≥32 bytes) with validation in `@PostConstruct`.
3. Use a secret manager (Vault, Kubernetes Secrets) for production.

---

### C-02: No Token Blacklist on Critical Operations

**File**: `mall-common/.../base/JwtAuthenticationFilter.java`
**Description**: The JWT filter validates token signature and expiry but does not check the Redis blacklist. While `AuthService.logout()` adds tokens to the blacklist, the filter does not consult it, meaning logged-out tokens remain valid until natural expiry (up to 24 hours).

**Impact**: Users who log out or whose tokens are revoked can still access the system for up to 24 hours.

**Remediation**:
1. Add Redis blacklist check in `JwtAuthenticationFilter` before setting SecurityContext.
2. Consider short-lived access tokens (15-30 minutes) with refresh token rotation.

---

### C-03: SQL Injection Risk in StockService

**File**: `mall-service/.../product/StockService.java:47,56,72,77`
**Description**: Stock quantity is concatenated directly into SQL via `.setSql("stock = stock - " + quantity)`:
```java
.setSql("stock = stock - " + quantity)
```
While `quantity` is an `int` (not string-concatenable for typical SQLi), this pattern is fragile and bypasses MyBatis-Plus's parameterized query mechanism. If a similar pattern were used with string parameters, it would be a direct injection vector.

**Impact**: Currently low risk due to `int` type, but the pattern sets a dangerous precedent. Future changes could introduce string concatenation in the same style.

**Remediation**:
1. Use MyBatis-Plus's `set()` with expression or custom mapper with `@Param`.
2. Alternatively, use a custom `UPDATE` mapper method with parameterized SQL:
   ```java
   @Update("UPDATE product_sku SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity}")
   int deductStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
   ```

---

## HIGH Findings

### H-01: CSRF Protection Disabled

**File**: `mall-common/.../base/SecurityConfig.java`
**Description**: CSRF is explicitly disabled:
```java
.csrf().disable()
```
While acceptable for pure API services with token-based auth, the system serves both API and potentially server-rendered content (Knife4j docs), increasing the attack surface.

**Impact**: A malicious site could trick authenticated users into making state-changing requests (POST/PUT/DELETE) via browser auto-included cookies (if session cookies are ever used).

**Remediation**:
1. If purely stateless JWT auth, document why CSRF is disabled.
2. If any cookie-based auth is used, re-enable CSRF with `CookieCsrfTokenRepository`.
3. Add `SameSite=Strict` to any cookies.

---

### H-02: Excessive Public Endpoint Exposure

**File**: `mall-common/.../base/SecurityConfig.java`
**Description**: Multiple endpoint groups are publicly accessible:
```java
.antMatchers("/api/auth/**").permitAll()
.antMatchers("/api/product/**").permitAll()
.antMatchers("/api/home/**").permitAll()
```
The `/api/product/**` wildcard exposes all product management endpoints (including create/update/delete) if not further restricted at the controller level.

**Impact**: Potential unauthorized access to product management operations if controller-level authorization is missing or inconsistent.

**Remediation**:
1. Split public and authenticated product endpoints: `/api/product/list`, `/api/product/detail` → public; `/api/product/create`, `/api/product/update` → authenticated.
2. Use method-level `@PreAuthorize` as defense-in-depth.
3. Audit all controllers under `/api/product/**` for proper authorization.

---

### H-03: ThreadLocal Context Leak Risk

**File**: `mall-common/.../base/JwtAuthenticationFilter.java`
**Description**: `TenantContext` and `UserContext` use `ThreadLocal` which is set in the JWT filter and cleared in a `finally` block. However:
1. If async operations (`@Async`, `CompletableFuture`) are used, ThreadLocal values are not propagated.
2. The `finally` block clears context but does not guard against servlet container thread pool reuse carrying stale data.

**Impact**: In async scenarios, `TenantContext.getTenantId()` may return null or the wrong tenant, causing data isolation failures.

**Remediation**:
1. Use `InheritableThreadLocal` or `TaskDecorator` for `@Async` thread pools.
2. Add a `ServletRequestListener` to clear context as a safety net.
3. Verify all async paths explicitly set TenantContext before data access.

---

### H-04: Payment Idempotency Relies on Status Check Only

**File**: `mall-service/.../payment/PaymentService.java:130-138`
**Description**: The `handleNotify` method checks payment status for idempotency but lacks distributed lock:
```java
if (record.getStatus() != PaymentStatusEnum.PENDING.getCode()) {
    return; // already processed
}
```
Under concurrent callbacks (e.g., Alipay retrying the same notification), two threads could both read `PENDING` and proceed.

**Impact**: Double payment processing under race conditions.

**Remediation**:
1. Add Redis distributed lock before status check:
   ```java
   String lockKey = RedisKeyConstant.PAYMENT_LOCK + paymentNo;
   boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
   if (!locked) return; // another thread is processing
   ```
2. Use database-level optimistic locking: `UPDATE ... WHERE status = PENDING` (already partially in `markPaymentSuccess`).

---

### H-05: No Rate Limiting on Authentication Endpoints

**File**: `mall-api/.../controller/AuthController.java` (assumed)
**Description**: Login and SMS endpoints lack rate limiting. While SMS has a 60-second interval and 5/day limit, the login endpoint itself has no brute-force protection.

**Impact**: Brute-force attacks on user passwords, credential stuffing.

**Remediation**:
1. Implement rate limiting on `/api/auth/login` (e.g., 5 failures per 15 minutes per IP).
2. Add account lockout after N consecutive failures.
3. Use Redis-based sliding window rate limiter.

---

## MEDIUM Findings

### M-01: BCrypt Cost Factor = 12

**File**: `mall-common/.../util/PasswordUtil.java`
**Description**: BCrypt cost factor of 12 is adequate for 2024 but may become insufficient as hardware improves. OWASP recommends cost ≥ 12, trending toward 13-14.

**Remediation**: Consider increasing to 13 for new passwords. Implement a migration strategy that re-hashes on next successful login.

---

### M-02: File Upload Local Storage

**File**: `mall-service/.../content/FileService.java`
**Description**: Files are stored on the local filesystem. While type (jpg/png/gif/webp) and size (5MB) validation exists, local storage has risks:
1. No virus/malware scanning for uploaded files.
2. File path traversal not explicitly validated.
3. No CDN for delivery performance.
4. Single point of failure — lost on server restart if not backed up.

**Remediation**:
1. Validate file content (magic bytes) in addition to extension.
2. Sanitize filenames and use UUID-based paths (already done).
3. Migrate to OSS (Aliyun OSS dependency is already in POM) for production.
4. Add anti-virus scanning for production.

---

### M-03: SMS Code Verification Lacks Brute-Force Protection

**File**: `mall-service/.../content/SmsService.java:87-108`
**Description**: The `verifyCode` method checks the code directly but does not limit the number of verification attempts. An attacker who intercepts a phone number could brute-force the 6-digit code (1M possibilities) without rate limiting.

**Remediation**:
1. Add max verification attempts (e.g., 5) with Redis counter.
2. Lock code after N failed attempts and require re-sending.

---

### M-04: Refresh Token Not Rotated

**File**: `mall-service/.../user/AuthService.java:163-196`
**Description**: The `refreshToken` method issues new access/refresh tokens but does not invalidate the old refresh token. If a refresh token is stolen, it can be used indefinitely until expiry (7 days).

**Remediation**:
1. Implement refresh token rotation: blacklist the old refresh token when issuing a new one.
2. Detect reuse of blacklisted refresh tokens as a security event (indicates token theft).

---

## LOW Findings

### L-01: Debug Logging of Sensitive Data

**File**: `mall-service/.../content/SmsService.java:77`
**Description**: Verification codes are logged:
```java
log.info("[SmsService] Mock send code: phone={}, code={}", phone, code);
```
This is acceptable for MVP/mock but must be removed in production.

**Remediation**: Remove code from log message or use `log.debug()` with production log level = INFO.

---

### L-02: WeChat Login Mock Implementation

**File**: `mall-service/.../user/AuthService.java:117-158`
**Description**: WeChat login uses a mock implementation that generates predictable OpenIDs (`wx_mock_{code}`). This must be replaced with real WeChat API integration before production.

**Remediation**: Implement real WeChat code-to-session exchange using `weixin-java-miniapp`.

---

### L-03: Order Timeout Consumer Lacks Idempotency

**File**: `mall-service/.../mq/consumer/OrderTimeoutConsumer.java`
**Description**: RabbitMQ consumer for order timeout should verify order status before cancelling, as the order may have been paid between timeout trigger and processing. The current implementation presumably checks, but this should be verified.

**Remediation**: Ensure consumer checks `orderStatus == PENDING_PAYMENT` before cancelling (optimistic lock pattern).

---

## Multi-Tenant Isolation Audit

| Check | Status | Notes |
|-------|--------|-------|
| SQL tenant injection via MyBatis-Plus | OK | `MallTenantLineHandler` injects `tenant_id` automatically |
| System table bypass | OK | `ignoreTable` list covers `sys_admin`, `sys_role`, etc. |
| Platform tenant ID (0) fallback | WARN | Returns `PLATFORM_TENANT_ID(0)` when no tenant — ensure platform queries don't leak cross-tenant data |
| API-level tenant validation | OK | Controller layer validates tenant access for merchant endpoints |
| ThreadLocal isolation | WARN | See H-03 regarding async scenarios |

---

## Payment Security Audit

| Check | Status | Notes |
|-------|--------|-------|
| Balance deduction with row lock | OK | `deductBalance` uses `WHERE balance >= amount` |
| Payment callback idempotency | WARN | See H-04 — needs distributed lock |
| Order-pay amount consistency | OK | Payment amount read from order record, not from request |
| Payment-to-order status atomicity | OK | Both updates in `markPaymentSuccess` within transaction |
| Refund amount validation | WARN | Should validate `refundAmount <= payAmount` before processing |

---

## Recommendations by Priority

1. **Immediate** (before any production exposure):
   - Fix C-01: Remove default JWT secret, enforce configuration
   - Fix C-02: Add blacklist check in JWT filter
   - Fix H-05: Add rate limiting on login

2. **Before production launch**:
   - Fix H-01: Document CSRF decision or re-enable
   - Fix H-02: Restrict public endpoint patterns
   - Fix H-04: Add distributed lock for payment callbacks
   - Fix H-03: Guard ThreadLocal in async contexts

3. **Post-launch hardening**:
   - Fix M-01 to M-04
   - Replace L-02 WeChat mock with real API
   - Remove L-01 debug logging
