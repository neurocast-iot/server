package com.neurocast.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.system.domain.SysPermission;
import com.neurocast.system.domain.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户的角色编码（通过 sys_user.role_id 外键）
     */
    @Select("""
            SELECT r.role_code
            FROM sys_role r
            INNER JOIN sys_user u ON u.role_id = r.id
            WHERE u.id = #{userId}
              AND r.status = 1
            """)
    List<String> selectRoleCodesByUserId(Long userId);

    /**
     * 查询角色已拥有的权限标识列表
     */
    @Select("""
            SELECT p.permission_code
            FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            WHERE rp.role_id = #{roleId}
            """)
    List<String> selectPermissionCodesByRoleId(Long roleId);

    /**
     * 查询角色已关联的权限实体列表（含完整字段）
     */
    @Select("""
            SELECT p.*
            FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            WHERE rp.role_id = #{roleId}
            ORDER BY p.id ASC
            """)
    List<SysPermission> selectPermissionsByRoleId(Long roleId);

    /**
     * 为角色分配权限
     */
    @Insert("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    int assignRolePermission(Long roleId, Long permissionId);

    /**
     * 删除角色的所有权限关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissions(Long roleId);

    /**
     * 从所有角色中删除指定权限的关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int deletePermissionFromAllRoles(Long permissionId);

    /**
     * 查询引用指定权限的角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role_permission WHERE permission_id = #{permissionId}")
    long countRolesByPermissionId(Long permissionId);
}
