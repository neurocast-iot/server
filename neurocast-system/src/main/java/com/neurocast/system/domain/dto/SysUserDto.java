package com.neurocast.system.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户创建/更新请求
 *
 * @param id       用户 ID（更新时必填）
 * @param username 登录用户名
 * @param password 密码（创建时必填，更新时为空表示不修改）
 * @param nickname 昵称
 * @param phone    手机号
 * @param email    邮箱
 * @param status   状态：1 启用 0 禁用
 * @param roleCode 角色编码：ADMIN / OPERATOR / VIEWER
 * @param remark   备注
 */
public record SysUserDto(
        Long id,

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 32, message = "用户名长度 3-32 位")
        String username,

        @Size(min = 6, max = 64, message = "密码长度 6-64 位")
        String password,

        @Size(max = 64, message = "昵称长度不能超过64")
        String nickname,

        @Size(max = 20, message = "手机号长度不能超过20")
        String phone,

        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过128")
        String email,

        Integer status,

        String roleCode,

        @Size(max = 256, message = "备注长度不能超过256")
        String remark
) {
}
