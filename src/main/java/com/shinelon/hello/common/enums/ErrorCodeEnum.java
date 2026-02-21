package com.shinelon.hello.common.enums;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author shinelon
 */
@Getter
public enum ErrorCodeEnum {

    /**
     * 成功
     */
    SUCCESS(200, "success"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 服务器内部错误
     */
    INTERNAL_ERROR(500, "服务器内部错误"),

    /**
     * AI服务不可用
     */
    AI_SERVICE_UNAVAILABLE(503, "AI服务不可用");

    private final int code;
    private final String message;

    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
