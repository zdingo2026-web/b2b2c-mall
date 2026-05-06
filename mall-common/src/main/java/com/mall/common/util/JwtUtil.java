package com.mall.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT utility for token generation and validation.
 * Supports three user types: platform, merchant, member.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${mall.jwt.secret}")
    private String secret;

    @Value("${mall.jwt.expiration:86400000}")
    private long expiration; // Default 24 hours in ms

    @Value("${mall.jwt.refresh-expiration:604800000}")
    private long refreshExpiration; // Default 7 days in ms

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("mall.jwt.secret must be configured");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("mall.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate access token.
     *
     * @param userId   user ID
     * @param userType user type (1=platform, 2=merchant, 3=member)
     * @param tenantId tenant ID (0 for platform, merchant ID for merchant, null for member)
     * @return JWT token string
     */
    public String generateToken(Long userId, int userType, Long tenantId) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("userId", userId);
        claims.put("userType", userType);
        if (tenantId != null) {
            claims.put("tenantId", tenantId);
        }
        return buildToken(claims, expiration);
    }

    /**
     * Generate refresh token.
     */
    public String generateRefreshToken(Long userId, int userType, Long tenantId) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("refresh", true);
        if (tenantId != null) {
            claims.put("tenantId", tenantId);
        }
        return buildToken(claims, refreshExpiration);
    }

    private String buildToken(Map<String, Object> claims, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Get all claims from token.
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get user ID from token.
     */
    public Long getUserId(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * Get user type from token.
     */
    public Integer getUserType(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userType = claims.get("userType");
        if (userType instanceof Integer) {
            return (Integer) userType;
        }
        return ((Number) userType).intValue();
    }

    /**
     * Get tenant ID from token.
     */
    public Long getTenantId(String token) {
        Claims claims = getClaimsFromToken(token);
        Object tenantId = claims.get("tenantId");
        if (tenantId == null) {
            return null;
        }
        if (tenantId instanceof Integer) {
            return ((Integer) tenantId).longValue();
        }
        return (Long) tenantId;
    }

    /**
     * Check if token is a refresh token.
     */
    public boolean isRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object refresh = claims.get("refresh");
        return Boolean.TRUE.equals(refresh);
    }

    /**
     * Validate token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public long getExpiration() {
        return expiration;
    }
}
