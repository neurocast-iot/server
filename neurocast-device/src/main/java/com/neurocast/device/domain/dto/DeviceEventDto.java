package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 设备事件基类。
 * <p>
 * 新增事件类型时，创建子类 DTO 并在 DeviceEventListener 中补充 switch 分支。
 */
@Getter
@Setter
public abstract class DeviceEventDto {

    /**
     * 事件类型标识（如 "device.connected"、"device.disconnected"）
     */
    @NotBlank(message = "type 不能为空")
    private String type;
}
