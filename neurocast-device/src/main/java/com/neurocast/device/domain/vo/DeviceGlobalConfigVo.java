package com.neurocast.device.domain.vo;

import com.neurocast.device.domain.DeviceGlobalConfig;

/**
 * 设备全局配置项（查询返回）
 */
public record DeviceGlobalConfigVo(
        String key,
        String value,
        String description
) {

    /**
     * 从 DB 记录构建
     */
    public static DeviceGlobalConfigVo from(DeviceGlobalConfig config) {
        return new DeviceGlobalConfigVo(config.getConfigKey(), config.getConfigValue(), config.getDescription());
    }
}
