package com.neurocast.system.service;

import com.neurocast.system.domain.dto.SysRoleDto;
import com.neurocast.system.domain.vo.SysPermissionVo;
import com.neurocast.system.domain.vo.SysRoleVo;

import java.util.List;

/**
 * 系统角色服务接口
 */
public interface SysRoleService {

    /**
     * 查询全部启用角色（用于下拉选择）
     */
    List<SysRoleVo> listEnabled();

    /**
     * 创建角色
     */
    void create(SysRoleDto dto);

    /**
     * 更新角色
     */
    void update(SysRoleDto dto);

    /**
     * 删除角色（同时清理用户关联和权限关联）
     */
    void delete(Long roleId);

    /**
     * 查询角色的权限列表（含完整字段）
     */
    List<SysPermissionVo> getRolePermissions(Long roleId);

    /**
     * 为角色分配权限（全量替换）
     *
     * @param roleId        角色 ID
     * @param permissionIds 权限 ID 列表，空列表表示清空该角色的所有权限
     */
    void assignRolePermissions(Long roleId, List<Long> permissionIds);
}
