package com.neurocast.common.core.constant;

import lombok.Getter;

/**
 * OTA 包类型
 */
@Getter
public enum OtaType {

    /**
     * 固件
     */
    FIRMWARE("fw", "FIRMWARE"),

    /**
     * 软件
     */
    SOFTWARE("sw", "SOFTWARE");

    /**
     * 简写码（接口层使用）
     */
    private final String shortCode;

    /**
     * ThingsBoard 完整类型名
     */
    private final String fullType;

    OtaType(String shortCode, String fullType) {
        this.shortCode = shortCode;
        this.fullType = fullType;
    }

    /**
     * 根据简写码或完整类型名解析，解析失败抛出 IllegalArgumentException
     */
    public static OtaType fromValue(String value) {
        for (OtaType otaType : values()) {
            if (otaType.shortCode.equalsIgnoreCase(value) || otaType.fullType.equalsIgnoreCase(value)) {
                return otaType;
            }
        }
        throw new IllegalArgumentException("未知的 OTA 类型：" + value);
    }
}
