package com.neurocast.framework.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 认证主体：JWT 用户、API 客户端、移动端用户
 */
@Getter
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID（JWT 用户为 sys_user.id，API 客户端为 api_client.id，会员为 member_user.id）
     */
    private final Long userId;

    /**
     * 用户名或客户端编码
     */
    private final String username;

    /**
     * 角色编码（仅 admin 用户有效）
     */
    private final String roleCode;

    /**
     * 主体类型：admin / api-client / member
     */
    private final String type;

    /**
     * API 客户端的权限范围（仅 api-client 类型有效）
     */
    private final Set<String> scopes;

    /**
     * 用户权限标识集合（仅 admin 类型有效，登录时从角色关联加载）
     */
    private final Set<String> permissions;

    private LoginUser(Long userId, String username, String roleCode, String type,
                      Set<String> scopes, Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.roleCode = roleCode;
        this.type = type;
        this.scopes = scopes;
        this.permissions = permissions;
    }

    /** 管理后台用户 */
    public static LoginUser ofUser(Long userId, String username, String roleCode, Set<String> permissions) {
        return new LoginUser(userId, username, roleCode, "admin", Set.of(),
                permissions != null ? permissions : Set.of());
    }

    /** API 客户端 */
    public static LoginUser ofApiClient(Long clientId, String clientCode, Set<String> scopes) {
        return new LoginUser(clientId, clientCode, null, "api-client",
                scopes != null ? scopes : Set.of(), Set.of());
    }

    /** 移动端会员用户 */
    public static LoginUser ofMember(Long memberId, String username) {
        return new LoginUser(memberId, username, null, "member", Set.of(), Set.of());
    }

    /**
     * Spring Security 权限列表：ROLE_{roleCode}（仅 admin 用户）
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roleCode != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
        }
        return List.of();
    }
}
