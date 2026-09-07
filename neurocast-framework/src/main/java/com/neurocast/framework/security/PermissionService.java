package com.neurocast.framework.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 统一权限校验服务，供 @PreAuthorize("@ss.hasPermission(...)") 等表达式调用。
 * <p>
 * 支持三种主体类型：
 * <ul>
 *   <li>admin — 管理后台用户，基于权限标识集合（登录时从角色关联加载）</li>
 *   <li>api-client — 第三方服务，基于 scope 集合</li>
 *   <li>member — 移动端用户，仅需认证</li>
 * </ul>
 * 权限标识统一存储在 sys_permission 表，用户权限和 API Scope 共用同一份定义。
 */
public class PermissionService {

    /**
     * 统一权限判断：admin 检查 permissions 集合，api-client 检查 scopes 集合
     * <p>
     * 权限标识定义在 sys_permission 表，admin 通过角色继承，api-client 通过 api_client_scope 分配。
     */
    public boolean hasPermission(String permission) {
        LoginUser user = tryGetLoginUser();
        if (user == null) {
            return false;
        }
        return switch (user.getType()) {
            case "admin"      -> user.getPermissions().contains(permission);
            case "api-client" -> user.getScopes().contains(permission);
            default           -> false;
        };
    }

    /**
     * API 客户端 scope 判断（向后兼容）
     */
    public boolean hasScope(String scope) {
        LoginUser user = tryGetLoginUser();
        if (user == null || !"api-client".equals(user.getType())) {
            return false;
        }
        return user.getScopes().contains(scope);
    }

    /**
     * 角色判断（向后兼容）
     */
    public boolean hasRole(String role) {
        LoginUser user = tryGetLoginUser();
        if (user == null) {
            return false;
        }
        return role.equals(user.getRoleCode());
    }

    /**
     * 任意角色匹配
     */
    public boolean hasAnyRole(String... roles) {
        LoginUser user = tryGetLoginUser();
        if (user == null || user.getRoleCode() == null) {
            return false;
        }
        for (String role : roles) {
            if (role.equals(user.getRoleCode())) {
                return true;
            }
        }
        return false;
    }

    private LoginUser tryGetLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }
}
