package com.neurocast.device.domain.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 设备全局配置新增/更新请求（批量提交，仅传需要新增或变更的配置项）
 */
public record DeviceGlobalConfigUpdateDto(
        @NotEmpty(message = "配置项不能为空") @Valid List<Item> items
) {
    public record Item(
            @NotBlank(message = "配置键不能为空")
            @Pattern(regexp = "^[a-z][a-z0-9_]{0,63}$",
                    message = "配置键格式不合法：小写字母开头，仅允许小写字母/数字/下划线，最长 64 位")
            String key,
            @NotBlank(message = "配置值不能为空")
            @Size(max = 512, message = "配置值最长 512 位")
            String value,
            @Size(max = 256, message = "配置描述最长 256 位")
            String description
    ) {
    }
}
