package com.neurocast.common.exception;

import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.ConstantsCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常基类。
 * 抛出时携带状态码，由全局异常处理器统一转换为 Result 响应。
 */
@Getter
public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private final Integer code;

    public ServiceException(String message) {
        super(message);
        this.code = ApiStatus.FAILED.getCode();
    }

    public ServiceException(ConstantsCode constantsCode) {
        super(constantsCode.getMsg());
        this.code = constantsCode.getCode();
    }

    public ServiceException(ConstantsCode constantsCode, String message) {
        super(message);
        this.code = constantsCode.getCode();
    }

    public ServiceException(ConstantsCode constantsCode, String message, Throwable cause) {
        super(message, cause);
        this.code = constantsCode.getCode();
    }
}
