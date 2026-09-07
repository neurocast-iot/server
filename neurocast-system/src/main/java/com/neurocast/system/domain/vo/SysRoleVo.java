package com.neurocast.system.domain.vo;

import com.neurocast.system.domain.SysRole;

/**
 * 角色响应
 *
 * @param id       角色 ID
 * @param roleCode 角色编码，如 ADMIN
 * @param roleName 角色名称，如 管理员
 * @param active   是否启用
 * @param remark   备注
 */
public record SysRoleVo(
        Long id,
        String roleCode,
        String roleName,
        Boolean active,
        String remark
) {
    /**
     * 实体转 VO
     *
     * <p>存储层 status=1/0 转换为契约层 active=true/false
     */
    public static SysRoleVo from(SysRole entity) {
        return new SysRoleVo(
                entity.getId(),
                entity.getRoleCode(),
                entity.getRoleName(),
                entity.getStatus() == 1,
                entity.getRemark()
        );
    }
}
