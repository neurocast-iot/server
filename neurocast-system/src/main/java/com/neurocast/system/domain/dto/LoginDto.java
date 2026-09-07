package com.neurocast.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求
 *
 * @param username 用户名
 * @param password 密码
 */
public record LoginDto(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 32, message = "用户名长度 3-32 位")
        String username,

        @NotBlank(message = "密码不能为空")
        String password
) {
}
