package com.neurocast.common.core.constant;

/**
 * ThingsBoard 属性作用域
 */
public interface TbScope {

    /**
     * 服务端作用域
     */
    String SERVER_SCOPE = "SERVER_SCOPE";

    /**
     * 共享作用域（服务端下发，设备可读）
     */
    String SHARED_SCOPE = "SHARED_SCOPE";

    /**
     * 客户端作用域
     */
    String CLIENT_SCOPE = "CLIENT_SCOPE";
}
