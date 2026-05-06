package com.mall.common.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Password utility using BCrypt.
 */
public final class PasswordUtil {

    private PasswordUtil() {}

    private static final int COST = 12;

    /**
     * Hash a plain-text password using BCrypt.
     */
    public static String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }

    /**
     * Verify a plain-text password against a BCrypt hash.
     */
    public static boolean verify(String rawPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
