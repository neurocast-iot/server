package com.neurocast.system.constant;

/**
 * 角色编码常量
 */
public interface RoleCode {

    /**
     * 管理员：全部权限
     */
    String ADMIN = "ADMIN";

    /**
     * 操作员：设备/媒体/OTA 操作权限
     */
    String OPERATOR = "OPERATOR";

    /**
     * 观察者：只读权限
     */
    String VIEWER = "VIEWER";
}
