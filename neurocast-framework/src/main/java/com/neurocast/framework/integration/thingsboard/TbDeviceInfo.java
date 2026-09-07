package com.neurocast.framework.integration.thingsboard;

import lombok.Getter;
import lombok.Setter;

/**
 * ThingsBoard 设备信息（防腐层 DTO，避免业务层直接依赖 TB 模型）
 */
@Getter
@Setter
public class TbDeviceInfo {

    private String deviceId;

    private String deviceName;

    private String deviceType;

    private String deviceProfileId;

    private String firmwareId;

    private String softwareId;

    private Long version;
}
