package com.neurocast.media.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备分片上传完成请求（S3/OSS 风格）。
 * uploadId 从 URL 路径读取，body 仅传 fileHash。
 */
@Getter
@Setter
public class DeviceUploadCompleteDto {

    /**
     * 上传任务 ID（由 Controller 从 URL 路径注入，设备端无需传）
     */
    private String uploadId;

    /**
     * 文件 MD5（可选，用于合并后校验）
     */
    private String fileHash;
}
