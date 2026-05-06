package com.mall.common.exception;

import com.mall.common.response.ResultCode;
import lombok.Getter;

/**
 * Parameter validation exception.
 */
@Getter
public class ParamException extends RuntimeException {

    private final int code;

    public ParamException(String message) {
        super(message);
        this.code = ResultCode.PARAM_ERROR.getCode();
    }

    public ParamException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ParamException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }
}
