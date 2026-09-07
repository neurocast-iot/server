package com.neurocast.common.utils;

import java.util.UUID;

/**
 * ID 工具
 */
public final class IdUtils {

    private IdUtils() {
    }

    /**
     * 生成不带横线的 UUID
     */
    public static String simpleUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
