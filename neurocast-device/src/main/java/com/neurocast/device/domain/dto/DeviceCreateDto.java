package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建设备请求
 */
@Getter
@Setter
public class DeviceCreateDto {

    /**
     * 设备唯一标识
     */
    @NotBlank(message = "设备 Uid 不能为空")
    private String deviceUid;

    /**
     * 设备名称（展示用，默认同 deviceUid）
     */
    private String name;

    /**
     * 所属产品 ID
     */
    @NotBlank(message = "产品 ID 不能为空")
    private String productId;
}
