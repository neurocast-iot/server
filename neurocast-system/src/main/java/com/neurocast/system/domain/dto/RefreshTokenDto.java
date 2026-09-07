package com.neurocast.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌请求
 *
 * @param refreshToken 刷新令牌
 */
public record RefreshTokenDto(
        @NotBlank(message = "刷新令牌不能为空")
        String refreshToken
) {
}
