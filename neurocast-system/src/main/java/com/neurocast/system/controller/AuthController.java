package com.neurocast.system.controller;

import com.neurocast.common.core.domain.Result;
import com.neurocast.system.domain.dto.LoginDto;
import com.neurocast.system.domain.dto.RefreshTokenDto;
import com.neurocast.system.domain.vo.LoginVo;
import com.neurocast.system.domain.vo.SysUserVo;
import com.neurocast.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证接口：登录/登出/刷新令牌/当前用户信息
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginDto loginDto) {
        return Result.ok(authService.login(loginDto));
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public Result<LoginVo> refresh(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        return Result.ok(authService.refresh(refreshTokenDto.refreshToken()));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return Result.ok();
    }

    /**
     * 当前登录用户信息
     */
    @GetMapping("/userinfo")
    public Result<SysUserVo> userinfo() {
        return Result.ok(authService.getUserInfo());
    }

    /**
     * 当前用户权限标识列表（前端根据此列表控制菜单显隐）
     */
    @GetMapping("/permissions")
    public Result<List<String>> permissions() {
        return Result.ok(authService.getUserPermissions());
    }
}
