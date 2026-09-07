package com.neurocast.system.domain.vo;

import com.neurocast.system.domain.SysRole;
import com.neurocast.system.domain.SysUser;

import java.time.OffsetDateTime;

/**
 * 用户信息响应
 *
 * @param id         用户 ID
 * @param username   登录用户名
 * @param nickname   昵称
 * @param phone      手机号
 * @param email      邮箱
 * @param roleCode   角色编码
 * @param roleName   角色名称
 * @param active     是否启用
 * @param createTime 创建时间
 */
public record SysUserVo(
        Long id,
        String username,
        String nickname,
        String phone,
        String email,
        String roleCode,
        String roleName,
        Boolean active,
        OffsetDateTime createTime
) {
    /**
     * 实体转 VO（无角色信息）
     */
    public static SysUserVo from(SysUser entity) {
        return new SysUserVo(
                entity.getId(),
                entity.getUsername(),
                entity.getNickname(),
                entity.getPhone(),
                entity.getEmail(),
                null, null,
                entity.getStatus() == 1,
                entity.getCreateTime()
        );
    }

    /**
     * 实体 + 角色转 VO
     */
    public static SysUserVo from(SysUser entity, SysRole role) {
        return new SysUserVo(
                entity.getId(),
                entity.getUsername(),
                entity.getNickname(),
                entity.getPhone(),
                entity.getEmail(),
                role != null ? role.getRoleCode() : null,
                role != null ? role.getRoleName() : null,
                entity.getStatus() == 1,
                entity.getCreateTime()
        );
    }
}
