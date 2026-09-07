package com.neurocast.media.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备文件上传结果
 */
@Getter
@Setter
public class DeviceUploadResultVo {

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 相对存储路径
     */
    private String filePath;

    /**
     * 文件 MD5
     */
    private String fileHash;
}
