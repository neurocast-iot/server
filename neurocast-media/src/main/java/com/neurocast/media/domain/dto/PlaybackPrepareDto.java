package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 回放触发单个文件上传请求
 */
public record PlaybackPrepareDto(
        /** 文件 ID（camera_video.id） */
        @NotBlank(message = "文件 ID 不能为空") String fileId
) {}
