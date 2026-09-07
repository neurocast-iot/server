package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 设备分片上传初始化请求（S3/OSS 风格）
 */
@Getter
@Setter
public class DeviceUploadInitDto {

    /**
     * 设备唯一标识
     */
    @NotBlank(message = "deviceUid 不能为空")
    private String deviceUid;

    /**
     * 文件名
     */
    @NotBlank(message = "filename 不能为空")
    private String filename;

    /**
     * 文件总大小（字节）
     */
    @NotNull(message = "fileSize 不能为空")
    private Long fileSize;

    /**
     * 文件 MD5（可选，用于秒传与合并校验）
     */
    private String fileHash;

    /**
     * 分片总数
     */
    @NotNull(message = "totalParts 不能为空")
    private Integer totalParts;
}
