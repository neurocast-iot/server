package com.neurocast.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.system.domain.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 权限标识 Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 查询用户拥有的权限标识列表（通过 sys_user.role_id 外键关联）
     */
    @Select("""
            SELECT DISTINCT p.permission_code
            FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            INNER JOIN sys_user u ON u.role_id = rp.role_id
            WHERE u.id = #{userId}
              AND p.status = 1
            """)
    List<String> selectPermissionCodesByUserId(Long userId);

    /**
     * 根据权限标识列表查询权限 ID 映射
     */
    @Select("""
            SELECT id, permission_code
            FROM sys_permission
            WHERE permission_code IN
            <script>
            <foreach collection='codes' item='code' open='(' separator=',' close=')'>
            #{code}
            </foreach>
            </script>
            """)
    List<SysPermission> selectByPermissionCodes(List<String> codes);

    /**
     * 查询所有已注册的 API Scope 编码（type=2）
     */
    @Select("""
            SELECT permission_code
            FROM sys_permission
            WHERE type = 2 AND status = 1
            """)
    List<String> selectAllApiScopeCodes();
}
