package com.mall.common.exception;

import com.mall.common.response.ResultCode;
import lombok.Getter;

/**
 * Authentication / authorization exception.
 */
@Getter
public class AuthException extends RuntimeException {

    private final int code;

    public AuthException(String message) {
        super(message);
        this.code = ResultCode.UNAUTHORIZED.getCode();
    }

    public AuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AuthException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }
}
