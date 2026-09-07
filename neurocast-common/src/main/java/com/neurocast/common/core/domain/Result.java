package com.neurocast.common.core.domain;

import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.ConstantsCode;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    public static <T> Result<T> ok() {
        return restResult(null, ApiStatus.SUCCESS.getCode(), null);
    }

    public static <T> Result<T> ok(T data) {
        return restResult(data, ApiStatus.SUCCESS.getCode(), null);
    }

    public static <T> Result<T> failed() {
        return restResult(null, ApiStatus.FAILED.getCode(), ApiStatus.FAILED.getMsg());
    }

    public static <T> Result<T> failed(String msg) {
        return restResult(null, ApiStatus.FAILED.getCode(), msg);
    }

    public static <T> Result<T> failed(ConstantsCode constantsCode) {
        return restResult(null, constantsCode.getCode(), constantsCode.getMsg());
    }

    public static <T> Result<T> failed(ConstantsCode constantsCode, String message) {
        return restResult(null, constantsCode.getCode(), message);
    }

    public static <T> Result<T> failed(Integer code, String message) {
        return restResult(null, code, message);
    }

    /**
     * 是否成功（仅服务端内部判断使用，code==0 即成功，不序列化到响应体）
     */
    @JSONField(serialize = false)
    public boolean isSuccess() {
        return ApiStatus.SUCCESS.getCode().equals(code);
    }

    private static <T> Result<T> restResult(T data, int code, String msg) {
        Result<T> apiResult = new Result<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMessage(msg);
        return apiResult;
    }
}
