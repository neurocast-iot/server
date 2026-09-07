package com.neurocast.media.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 录像记录
 */
@Getter
@Setter
@TableName("camera_video")
public class CameraVideo extends BaseEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 设备 Uid
     */
    private String deviceUid;

    /**
     * ThingsBoard 设备 ID
     */
    private String tbDeviceId;

    /**
     * 文件名
     */
    private String name;

    /**
     * 事件时间（秒级时间戳）
     */
    private Long eventTime;

    /**
     * 文件数据的实际起始时间（秒级时间戳）
     */
    private Long startTime;

    /**
     * 录像时长（秒）
     */
    private Integer duration;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 上传状态，见 FileStatus
     */
    private Integer status;

    /**
     * HLS 分片状态，见 HlsStatus（0=pending, 1=processing, 2=ready, 3=failed）
     */
    private Integer hlsStatus;

    /**
     * 触发类型：timer / bluetooth / record
     */
    private String triggerType;

    /**
     * 设备端文件路径
     */
    private String filePath;

    /**
     * 缩略图文件名
     */
    private String thumbName;

    /**
     * 缩略图设备端路径
     */
    private String thumbPath;

    /**
     * 缩略图文件大小（字节）
     */
    private Long thumbSize;
}
