package com.neurocast.device.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备业务配置（来自 SHARED_SCOPE 属性）
 */
@Getter
@Setter
public class DeviceConfigVo {

    // ── 元数据 ──
    private Long lastUpdatedTime;

    // ── 主通道（视频） ──
    private String mainCodec;
    private Integer mainFrameRate;
    private Integer mainResolutionWidth;
    private Integer mainResolutionHeight;
    private Integer mainBitrate;
    private String mainBrMode;

    // ── 子通道（抓拍） ──
    private String subCodec;
    private Integer subFrameRate;
    private Integer subResolutionWidth;
    private Integer subResolutionHeight;
    private Integer subBitrate;
    private String subBrMode;
    private Integer snapshotQuality;

    // ── 录像 ──
    private Integer recordSegmentSec;
}
