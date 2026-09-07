package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 分片合并请求
 */
@Getter
@Setter
public class FileMergeDto {

    @NotBlank(message = "上传任务 ID 不能为空")
    private String uploadId;

    /**
     * 文件 MD5（用于合并后校验）
     */
    private String fileHash;
}
