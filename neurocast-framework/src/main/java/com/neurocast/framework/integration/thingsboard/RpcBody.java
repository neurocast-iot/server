package com.neurocast.framework.integration.thingsboard;

import lombok.Getter;
import lombok.Setter;

/**
 * ThingsBoard RPC 请求体
 */
@Getter
@Setter
public class RpcBody<T> {

    /**
     * RPC 方法名，如 push_camera_stream、restart
     */
    private String method;

    /**
     * RPC 参数
     */
    private T params;

    /**
     * 设备离线时是否持久化（设备上线后补发）
     */
    private Boolean persistent = true;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 5000;

    public static <T> RpcBody<T> of(String method, T params) {
        RpcBody<T> body = new RpcBody<>();
        body.setMethod(method);
        body.setParams(params);
        return body;
    }
}
