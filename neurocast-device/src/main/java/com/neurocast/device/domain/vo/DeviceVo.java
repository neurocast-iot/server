package com.neurocast.device.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * 设备信息（含 TB 遥测版本号）
 */
@Getter
@Setter
public class DeviceVo {

    private String id;

    private String deviceUid;

    private String tbDeviceId;

    private String name;

    /**
     * 在线状态：1 在线 0 离线
     */
    private Integer status;

    private OffsetDateTime lastedOnlineTime;

    private OffsetDateTime lastedOfflineTime;

    /**
     * 当前固件版本（来自 TB 遥测 current_fw_version）
     */
    private String currentFwVersion;

    /**
     * 当前软件版本（来自 TB 遥测 current_sw_version）
     */
    private String currentSwVersion;

    private OffsetDateTime createTime;

    /**
     * 关联的产品信息
     */
    private ProductInfo product;

    /**
     * 产品摘要信息
     */
    @Getter
    @Setter
    public static class ProductInfo {

        private String id;

        private String name;

        private String model;
    }
}
