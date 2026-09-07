package com.neurocast.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 角色创建/更新请求
 *
 * @param id       角色 ID（更新时必填）
 * @param roleCode 角色编码（如 ADMIN）
 * @param roleName 角色名称（如 管理员）
 * @param status   状态：1 启用 0 禁用
 * @param remark   备注
 */
public record SysRoleDto(
        Long id,
        @NotBlank(message = "角色编码不能为空")
        @Size(max = 32, message = "角色编码长度不能超过32")
        String roleCode,
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 64, message = "角色名称长度不能超过64")
        String roleName,
        Integer status,
        @Size(max = 256, message = "备注长度不能超过256")
        String remark
) {}
