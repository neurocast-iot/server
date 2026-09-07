package com.neurocast.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 权限标识创建/更新请求
 *
 * @param permissionCode 权限标识，如 system:user:list、device:read
 * @param description    权限描述
 * @param type           权限类型：1=用户权限（RBAC），2=API Scope（客户端），默认 1
 */
public record SysPermissionDto(
        @NotBlank(message = "权限标识不能为空")
        @Size(max = 128, message = "权限标识长度不能超过128")
        String permissionCode,

        @Size(max = 256, message = "描述长度不能超过256")
        String description,

        Integer type
) {
}
