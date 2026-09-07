package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 录像事件回传，字段与 TB 规则引擎脚本输出对齐
 */
@Getter
@Setter
public class DataCameraVideoDto {

    @NotBlank(message = "tbDeviceId 不能为空")
    private String tbDeviceId;

    /** 文件名 */
    private String fileName;

    /** 事件时间（秒级时间戳） */
    private Long eventTime;

    /** 文件数据的实际起始时间（秒级时间戳） */
    private Long startTime;

    /** 录像时长（秒） */
    private Integer duration;

    /** 触发类型：timer / bluetooth / record */
    private String triggerType;

    /** 设备端文件路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 缩略图文件名 */
    private String thumbName;

    /** 缩略图设备端路径 */
    private String thumbPath;

    /** 缩略图文件大小（字节） */
    private Long thumbSize;
}
