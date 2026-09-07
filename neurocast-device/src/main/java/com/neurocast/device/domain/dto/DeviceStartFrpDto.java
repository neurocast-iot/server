package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 启动 FRP 内网穿透请求（SSH 反向代理）
 */
@Getter
@Setter
public class DeviceStartFrpDto {

    @NotBlank(message = "设备 Uid 不能为空")
    private String deviceUid;

    /**
     * 设备侧 SSH 本地 IP
     */
    @NotBlank(message = "SSH 本地 IP 不能为空")
    private String sshLocalIp;

    /**
     * 设备侧 SSH 本地端口
     */
    @NotNull(message = "SSH 本地端口不能为空")
    private Integer sshLocalPort;
}
