package com.neurocast.media.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * 录像记录视图
 */
@Getter
@Setter
public class CameraVideoVo {

    private String id;

    private String deviceUid;

    private String tbDeviceId;

    private String name;

    private Long eventTime;

    /** 文件数据的实际起始时间（秒级时间戳） */
    private Long startTime;

    /** 录像时长（秒） */
    private Integer duration;

    private Integer status;

    private Long fileSize;

    /** 触发类型：timer / bluetooth / record */
    private String triggerType;

    /** 设备端文件路径 */
    private String filePath;

    /** 缩略图文件名 */
    private String thumbName;

    /** 缩略图设备端路径 */
    private String thumbPath;

    /** 缩略图文件大小（字节） */
    private Long thumbSize;

    /** 下载链接 */
    private String fileUrl;

    /** 缩略图链接（有缩略图时填充） */
    private String thumbUrl;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;
}
