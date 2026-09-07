package com.neurocast.device.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 设备 RPC 指令协议：全工程唯一定义 RPC 方法名的地方。
 * 新增设备指令时在此登记，禁止在业务代码中散落字符串。
 */
@Getter
@RequiredArgsConstructor
public enum DeviceRpcMethod {

    /**
     * 实时直播推流
     */
    PUSH_CAMERA_STREAM("startLiveStream", "开始实时流", true),

    /**
     * 停止实时推流
     */
    STOP_CAMERA_STREAM("stopLiveStream", "停止实时流", true),

    /**
     * 设备上传媒体文件（统一指令，通过 fileType 区分图片/录像）
     */
    UPLOAD_FILE("uploadFile", "上传媒体文件", true),

    /**
     * 启动 FRP 内网穿透
     */
    START_FRP("start_frp", "启动 FRP 穿透", true),

    /**
     * 停止 FRP 内网穿透
     */
    STOP_FRP("stop_frp", "停止 FRP 穿透", true),

    /**
     * 启动 SSH 隧道
     */
    START_SSH_TUNNEL("start_ssh_tunnel", "启动 SSH 隧道", true),

    /**
     * 停止 SSH 隧道
     */
    STOP_SSH_TUNNEL("stop_ssh_tunnel", "停止 SSH 隧道", true),

    /**
     * 复位设备：清除设备配置并自动重启（需设备应答，双向 RPC）
     */
    RESET("reset", "复位设备（清配置 + 自动重启）", false),

    /**
     * 重启设备（需设备应答，双向 RPC）
     */
    RESTART("restart", "重启设备", false);

    /**
     * TB RPC 方法名（与设备端协议一致，不可随意改动）
     */
    private final String method;

    /**
     * 中文描述
     */
    private final String description;

    /**
     * true 单向下发（无需回执）；false 双向下发（等待设备应答）
     */
    private final boolean oneWay;
}
