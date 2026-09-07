package com.neurocast.framework.security;

import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.exception.ServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前认证主体，未认证时抛出业务异常
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new ServiceException(ApiStatus.UNAUTHORIZED);
    }

    /**
     * 当前用户 ID
     */
    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    /**
     * 当前用户名（或 API 客户端编码）
     */
    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    /**
     * 当前角色编码
     */
    public static String getRoleCode() {
        return getLoginUser().getRoleCode();
    }
}
