package com.mall.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Unified result codes.
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(0, "success"),
    FAIL(-1, "fail"),

    // Param errors 1xxx
    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "参数缺失"),
    PARAM_TYPE_ERROR(1003, "参数类型错误"),

    // Auth errors 2xxx
    UNAUTHORIZED(2001, "未登录或token已过期"),
    FORBIDDEN(2002, "无权限访问"),
    TOKEN_INVALID(2003, "无效的Token"),
    TOKEN_EXPIRED(2004, "Token已过期"),
    LOGIN_FAIL(2005, "登录失败"),
    ACCOUNT_DISABLED(2006, "账号已禁用"),
    ACCOUNT_LOCKED(2007, "账号已锁定"),

    // Business errors 3xxx
    BUSINESS_ERROR(3001, "业务处理失败"),
    DATA_NOT_FOUND(3002, "数据不存在"),
    DATA_ALREADY_EXISTS(3003, "数据已存在"),
    DATA_SAVE_FAIL(3004, "数据保存失败"),
    DATA_UPDATE_FAIL(3005, "数据更新失败"),
    DATA_DELETE_FAIL(3006, "数据删除失败"),

    // User errors 4xxx
    USER_NOT_FOUND(4001, "用户不存在"),
    USER_PASSWORD_ERROR(4002, "密码错误"),
    USER_PHONE_EXISTS(4003, "手机号已注册"),
    USER_USERNAME_EXISTS(4004, "用户名已存在"),

    // Product errors 5xxx
    PRODUCT_NOT_FOUND(5001, "商品不存在"),
    PRODUCT_OFF_SHELF(5002, "商品已下架"),
    SKU_NOT_FOUND(5003, "SKU不存在"),
    STOCK_NOT_ENOUGH(5004, "库存不足"),

    // Order errors 6xxx
    ORDER_NOT_FOUND(6001, "订单不存在"),
    ORDER_STATUS_ERROR(6002, "订单状态异常"),
    ORDER_CANNOT_CANCEL(6003, "订单无法取消"),
    ORDER_CANNOT_PAY(6004, "订单无法支付"),

    // Payment errors 7xxx
    PAYMENT_FAIL(7001, "支付失败"),
    PAYMENT_TIMEOUT(7002, "支付超时"),
    REFUND_FAIL(7003, "退款失败"),

    // Tenant errors 8xxx
    TENANT_NOT_FOUND(8001, "商户不存在"),
    TENANT_DISABLED(8002, "商户已禁用"),
    TENANT_NOT_APPROVED(8003, "商户未审核通过"),

    // System errors 9xxx
    SYSTEM_ERROR(9001, "系统繁忙，请稍后重试"),
    FILE_UPLOAD_FAIL(9002, "文件上传失败"),
    SMS_SEND_FAIL(9003, "短信发送失败");

    private final int code;
    private final String msg;
}
