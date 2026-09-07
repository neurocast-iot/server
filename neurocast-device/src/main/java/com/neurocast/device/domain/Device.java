package com.neurocast.device.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.OffsetDateTime;

/**
 * 设备（与 ThingsBoard 设备一一对应）
 */
@Getter
@Setter
@TableName("device")
public class Device extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属产品 ID
     */
    private String productId;

    /**
     * ThingsBoard 设备 ID
     */
    private String tbDeviceId;

    /**
     * 设备唯一标识（与 TB 设备名一致，也是直播流名）
     */
    private String deviceUid;

    /**
     * 设备名称（展示用）
     */
    private String name;

    /**
     * 在线状态：1 在线 0 离线（由设备状态上报更新）
     */
    private Integer status;

    /**
     * 最近在线时间
     */
    private OffsetDateTime lastedOnlineTime;

    /**
     * 最近离线时间
     */
    private OffsetDateTime lastedOfflineTime;
}
