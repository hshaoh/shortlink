package com.saas.shortlink.admin.common.enums;

import com.saas.shortlink.admin.common.convention.errorcode.IErrorCode;

/**
 * 用户错误码
 */
public enum UserErrorCodeEnum implements IErrorCode {

    USER_NULL("A000200", "用户记录不存在"),

    USER_NAME_EXIST("A000201", "用户名已存在"),

    USER_EXIST("A000202", "用户记录已存在"),

    USER_SAVE_ERROR("A000203", "用户记录新增失败"),

    USER_LOGIN_ERROR("A000204", "用户登录错误"),

    USER_NOT_LOGIN("A000205","用户未登录或者用户token无效");

    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}