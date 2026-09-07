package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新设备请求（仅名称，在线状态由 TB 活动事件驱动）
 */
@Getter
@Setter
public class DeviceUpdateDto {

    @NotBlank(message = "设备 Uid 不能为空")
    private String deviceUid;

    /**
     * 设备名称
     */
    private String name;
}
