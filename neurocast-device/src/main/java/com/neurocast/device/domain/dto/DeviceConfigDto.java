package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 设备业务配置下发请求（与设备端 SHARED_SCOPE 属性键对应）
 */
@Getter
@Setter
public class DeviceConfigDto {

    /************ 主通道（视频） ************/

    /**
     * 视频编码格式，支持 h264 / h265
     */
    @Pattern(regexp = "^(h264|h265)$", message = "视频编码必须是 h264 或 h265")
    private String mainCodec;

    /**
     * 视频帧率，范围 5-30
     */
    @Min(value = 5, message = "视频帧率必须在 5-30 之间")
    @Max(value = 30, message = "视频帧率必须在 5-30 之间")
    private Integer mainFrameRate;

    /**
     * 视频分辨率宽度，支持: 320, 640, 768, 1024, 1280, 1920, 2560
     */
    private Integer mainResolutionWidth;

    /**
     * 视频分辨率高度，支持: 176, 360, 432, 576, 720, 1080, 1440
     */
    private Integer mainResolutionHeight;

    /**
     * 视频码率（kbps）
     */
    private Integer mainBitrate;

    /**
     * 主通道码率模式：CBR（固定码率）、VBR（可变码率）、AVBR（自适应可变码率）
     */
    @Pattern(regexp = "^(?i)(CBR|VBR|AVBR)$", message = "主通道码率模式必须是 CBR、VBR 或 AVBR")
    private String mainBrMode;

    /************ 录像 ************/

    /**
     * 录像分段时长（秒），范围 1-3600
     */
    @Min(value = 1, message = "录像分段时长必须在 1-3600 秒之间")
    @Max(value = 3600, message = "录像分段时长必须在 1-3600 秒之间")
    private Integer recordSegmentSec;

    /************ 子通道（抓拍） ************/

    /**
     * 子通道编码格式，支持 h264 / h265
     */
    @Pattern(regexp = "^(h264|h265)$", message = "子通道编码必须是 h264 或 h265")
    private String subCodec;

    /**
     * 子通道帧率，范围 5-30
     */
    @Min(value = 5, message = "子通道帧率必须在 5-30 之间")
    @Max(value = 30, message = "子通道帧率必须在 5-30 之间")
    private Integer subFrameRate;

    /**
     * 子通道分辨率宽度，支持: 320, 640, 768, 1024, 1280
     */
    private Integer subResolutionWidth;

    /**
     * 子通道分辨率高度，支持: 176, 360, 432, 576, 720
     */
    private Integer subResolutionHeight;

    /**
     * 子通道码率（kbps）
     */
    private Integer subBitrate;

    /**
     * 子通道码率模式：CBR（固定码率）、VBR（可变码率）、AVBR（自适应可变码率）
     */
    @Pattern(regexp = "^(?i)(CBR|VBR|AVBR)$", message = "子通道码率模式必须是 CBR、VBR 或 AVBR")
    private String subBrMode;

    /**
     * 图片质量，范围 20-100
     */
    @Min(value = 20, message = "图片质量必须在 20-100 之间")
    @Max(value = 100, message = "图片质量必须在 20-100 之间")
    private Integer snapshotQuality;
}
