package com.neurocast.system.service;

import com.neurocast.system.domain.dto.LoginDto;
import com.neurocast.system.domain.vo.LoginVo;
import com.neurocast.system.domain.vo.SysUserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 认证服务：登录/登出/刷新令牌/当前用户信息
 */
public interface AuthService {

    /**
     * 用户名密码登录
     */
    LoginVo login(LoginDto loginDto);

    /**
     * 使用刷新令牌换取新的访问令牌
     */
    LoginVo refresh(String refreshToken);

    /**
     * 登出：当前访问令牌加入黑名单并清除刷新令牌
     */
    void logout(HttpServletRequest request);

    /**
     * 查询当前登录用户信息
     */
    SysUserVo getUserInfo();

    /**
     * 查询当前用户权限标识列表
     */
    List<String> getUserPermissions();
}
