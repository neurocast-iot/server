package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 分片上传初始化请求
 */
@Getter
@Setter
public class FileUploadInitDto {

    @NotBlank(message = "文件名不能为空")
    private String filename;

    /**
     * 文件 MD5（用于秒传与合并校验）
     */
    private String fileHash;

    /**
     * 文件总大小（字节）
     */
    @NotNull(message = "文件大小不能为空")
    private Long totalSize;

    /**
     * 单个分片大小（字节）
     */
    private Long chunkSize;

    /**
     * 分片总数
     */
    @NotNull(message = "分片总数不能为空")
    private Integer totalChunks;

    /**
     * 存储子目录（可选）
     */
    private String subDir;
}
