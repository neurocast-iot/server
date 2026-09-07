package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 通用设备操作请求（reset/restart/stopFrp）
 */
@Getter
@Setter
public class DeviceUidDto {

    @NotBlank(message = "设备 Uid 不能为空")
    private String deviceUid;
}
