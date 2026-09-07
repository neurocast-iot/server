package com.neurocast.device.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * 设备指令记录
 */
@Getter
@Setter
public class DeviceCommandLogVo {

    private String id;

    /**
     * 设备唯一标识
     */
    private String deviceUid;

    /**
     * RPC 方法名
     */
    private String method;

    /**
     * 指令中文描述
     */
    private String description;

    /**
     * 下发参数（JSON）
     */
    private String params;

    /**
     * 指令状态：SENT / SUCCESS / FAILED
     */
    private String status;

    /**
     * 设备应答内容或失败原因
     */
    private String response;

    /**
     * 下发耗时（毫秒）
     */
    private Long costMs;

    /**
     * 下发时间
     */
    private OffsetDateTime createTime;

    /**
     * 操作人
     */
    private String createBy;
}
