package com.neurocast.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求
 *
 * @param newPassword 新密码
 */
public record ResetPasswordDto(
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度 6-64 位")
        String newPassword
) {
}
