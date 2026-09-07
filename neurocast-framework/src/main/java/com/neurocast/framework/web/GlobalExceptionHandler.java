package com.neurocast.framework.web;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.domain.Result;
import com.neurocast.common.exception.ServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器：统一转换为 Result 响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * TB 错误码 31：设备已存在
     */
    private static final int TB_ERROR_CODE_DEVICE_EXISTED = 31;

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public Result<Void> handleServiceException(ServiceException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.failed(e.getCode(), e.getMessage());
    }

    /**
     * @RequestBody 参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::fieldErrorMessage)
                .collect(Collectors.joining("; "));
        return Result.failed(ApiStatus.VALIDATE_FAILED, message);
    }

    /**
     * 表单绑定校验失败
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::fieldErrorMessage)
                .collect(Collectors.joining("; "));
        return Result.failed(ApiStatus.VALIDATE_FAILED, message);
    }

    /**
     * @RequestParam/@PathVariable 校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.failed(ApiStatus.VALIDATE_FAILED, message);
    }

    /**
     * 请求体解析失败
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.failed(ApiStatus.PARAMETER_FORMAT_ERROR, "请求体格式错误");
    }

    /**
     * 缺少必填参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return Result.failed(ApiStatus.VALIDATE_FAILED, "缺少必填参数：" + e.getParameterName());
    }

    /**
     * 请求方式不支持（如 POST 打到 GET 接口）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源未找到：{}", e.getResourcePath());
        return Result.failed(ApiStatus.HTTP_REQUEST_METHOD_NOT_SUPPORTED, "接口不存在或请求方式不支持");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return Result.failed(ApiStatus.HTTP_REQUEST_METHOD_NOT_SUPPORTED,
                "不支持的请求方式：" + e.getMethod());
    }

    /**
     * 无权限（认证通过但角色不足）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.failed(ApiStatus.FORBIDDEN);
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        return Result.failed(ApiStatus.UNAUTHORIZED);
    }

    /**
     * ThingsBoard HTTP 调用异常（正常情况已被 ClientManager 转换，此处兜底）
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public Result<Void> handleHttpClientErrorException(HttpClientErrorException e) {
        log.error("ThingsBoard 调用失败：status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        if (isThingsBoardDeviceExisted(e.getResponseBodyAsString())) {
            return Result.failed(ApiStatus.BUSINESS_THINGSBOARD_DEVICE_EXISTED);
        }
        return Result.failed(ApiStatus.BUSINESS_THINGSBOARD_ERROR, e.getResponseBodyAsString());
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.failed(ApiStatus.ERROR, "系统异常，请稍后重试");
    }

    private static String fieldErrorMessage(FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }

    /**
     * 判断 TB 响应是否为"设备已存在"错误（errorCode=31）
     */
    private boolean isThingsBoardDeviceExisted(String body) {
        if (body == null || body.isEmpty()) {
            return false;
        }
        try {
            JSONObject json = JSON.parseObject(body);
            return json.getIntValue("errorCode") == TB_ERROR_CODE_DEVICE_EXISTED;
        } catch (Exception e) {
            return false;
        }
    }
}
