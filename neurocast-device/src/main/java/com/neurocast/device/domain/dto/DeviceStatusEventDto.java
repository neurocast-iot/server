package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 设备上下线事件（type = "device.connected" 或 "device.disconnected"）。
 * <p>
 * 上下线由 type 本身区分，无需额外 status/active 字段。
 */
@Getter
@Setter
public class DeviceStatusEventDto extends DeviceEventDto {

    /**
     * TB 设备 ID
     */
    @NotBlank(message = "tbDeviceId 不能为空")
    private String tbDeviceId;
}
