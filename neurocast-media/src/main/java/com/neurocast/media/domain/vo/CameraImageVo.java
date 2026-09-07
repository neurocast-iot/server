package com.neurocast.media.domain.vo;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 抓拍图片记录视图
 */
@Getter
@Setter
public class CameraImageVo {

    private String id;

    private String deviceUid;

    private String tbDeviceId;

    private String name;

    private Long eventTime;

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
