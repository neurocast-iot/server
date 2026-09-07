package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 启动 SSH 隧道请求
 */
@Getter
@Setter
public class DeviceStartSshTunnelDto {

    @NotBlank(message = "设备 Uid 不能为空")
    private String deviceUid;

    /**
     * 设备侧本地 IP（要暴露的服务地址）
     */
    @NotBlank(message = "本地 IP 不能为空")
    private String localIp;

    /**
     * 设备侧本地端口（要暴露的服务端口）
     */
    @NotNull(message = "本地端口不能为空")
    private Integer localPort;
}
