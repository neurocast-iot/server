package com.neurocast.system.domain.vo;

import com.neurocast.system.domain.SysPermission;

import java.time.OffsetDateTime;

/**
 * 权限标识响应
 *
 * @param id             权限 ID
 * @param permissionCode 权限标识，如 system:user:list、device:read
 * @param description    描述
 * @param type           权限类型：1=用户权限，2=API Scope
 * @param active         是否启用
 * @param createTime     创建时间
 */
public record SysPermissionVo(
        Long id,
        String permissionCode,
        String description,
        Integer type,
        Boolean active,
        OffsetDateTime createTime
) {
    /**
     * 实体转 VO
     *
     * <p>存储层 status=1/0 转换为契约层 active=true/false，
     * 提升接口可读性，调用方无需理解编码含义。
     */
    public static SysPermissionVo from(SysPermission entity) {
        return new SysPermissionVo(
                entity.getId(),
                entity.getPermissionCode(),
                entity.getDescription(),
                entity.getType(),
                entity.getStatus() == 1,
                entity.getCreateTime()
        );
    }
}
