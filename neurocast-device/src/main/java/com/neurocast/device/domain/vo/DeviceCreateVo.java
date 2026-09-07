package com.neurocast.device.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备创建结果（accessToken 仅创建时返回一次）
 */
@Getter
@Setter
public class DeviceCreateVo {

    private String deviceUid;

    /**
     * 设备接入令牌（21 位，用于设备接入 ThingsBoard 与推流鉴权）
     */
    private String accessToken;
}
