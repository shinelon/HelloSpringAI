package com.shinelon.hello.common.exception;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import lombok.Getter;

/**
 * 业务异常类
 *
 * @author shinelon
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCodeEnum errorCode;

    /**
     * 自定义错误消息
     */
    private final String customMessage;

    public BusinessException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public BusinessException(ErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.customMessage = message;
    }

    public BusinessException(ErrorCodeEnum errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public BusinessException(ErrorCodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.customMessage = message;
    }

    /**
     * 获取错误消息
     */
    public String getErrorMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}
