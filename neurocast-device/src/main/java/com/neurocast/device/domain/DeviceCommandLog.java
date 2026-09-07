package com.neurocast.device.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 设备指令记录：每次下发留痕，双向指令记录设备应答结果
 */
@Getter
@Setter
@TableName("device_rpc_log")
public class DeviceCommandLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 指令状态：已下发（单向）
     */
    public static final String STATUS_SENT = "SENT";

    /**
     * 指令状态：设备应答成功（双向）
     */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * 指令状态：下发失败/设备应答失败
     */
    public static final String STATUS_FAILED = "FAILED";

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 设备唯一标识
     */
    private String deviceUid;

    /**
     * RPC 方法名（对应 DeviceRpcMethod）
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
     * 下发耗时（毫秒，双向指令为等待应答时长）
     */
    private Long costMs;
}
