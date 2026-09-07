package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 前端请求回放
 */
public record PlaybackRequestDto(
        @NotBlank(message = "设备 Uid 不能为空") String deviceUid,
        @NotNull(message = "起始时间不能为空") Long startTime,
        @NotNull(message = "结束时间不能为空") Long endTime
) {}
