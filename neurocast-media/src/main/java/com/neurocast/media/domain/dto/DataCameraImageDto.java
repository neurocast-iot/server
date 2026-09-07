package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 抓拍图片事件回传（单条），字段与 TB 规则引擎脚本输出对齐
 */
@Getter
@Setter
public class DataCameraImageDto {

    @NotBlank(message = "tbDeviceId 不能为空")
    private String tbDeviceId;

    /** 文件名 */
    private String fileName;

    /** 事件时间（秒级时间戳） */
    private Long eventTime;

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
