package com.neurocast.common.core.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ThingsBoard 遥测键（设备上报、跨模块共用：device 展示、ota 版本比较）
 */
@Getter
@RequiredArgsConstructor
public enum TbTelemetryKey {

    /**
     * 设备当前固件版本
     */
    CURRENT_FW_VERSION("current_fw_version", "当前固件版本"),

    /**
     * 设备当前软件版本
     */
    CURRENT_SW_VERSION("current_sw_version", "当前软件版本");

    /**
     * TB 遥测键名（与设备端协议一致，不可随意改动）
     */
    private final String key;

    /**
     * 中文描述
     */
    private final String description;
}
