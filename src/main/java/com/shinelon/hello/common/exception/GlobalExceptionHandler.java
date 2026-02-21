package com.shinelon.hello.common.exception;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author shinelon
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: code={}, message={}",
                e.getErrorCode().getCode(), e.getErrorMessage());

        Result<Void> result = Result.error(e.getErrorCode(), e.getErrorMessage());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);

        Result<Void> result = Result.error(ErrorCodeEnum.PARAM_ERROR, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理参数校验异常（@RequestParam）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", errorMessage);

        Result<Void> result = Result.error(ErrorCodeEnum.PARAM_ERROR, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理请求体解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Request body not readable: {}", e.getMessage());

        String errorMessage = "请求体格式错误";

        String exceptionMessage = e.getMessage();
        if (exceptionMessage != null) {
            if (exceptionMessage.contains("Required request body is missing")) {
                errorMessage = "请求体不能为空";
            } else if (exceptionMessage.contains("JSON parse error")) {
                errorMessage = "JSON格式错误";
            } else if (exceptionMessage.contains("conversationId")) {
                errorMessage = "会话ID不能为空";
            } else if (exceptionMessage.contains("content")) {
                errorMessage = "消息内容不能为空";
            }
        }

        Result<Void> result = Result.error(ErrorCodeEnum.PARAM_ERROR, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理 IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());

        Result<Void> result = Result.error(ErrorCodeEnum.PARAM_ERROR, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);

        Result<Void> result = Result.error(ErrorCodeEnum.INTERNAL_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
