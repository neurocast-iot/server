package com.neurocast.media.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 抓拍图片记录
 */
@Getter
@Setter
@TableName("camera_image")
public class CameraImage extends BaseEntity {

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
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 上传状态，见 FileStatus
     */
    private Integer status;

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
