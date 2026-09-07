package com.neurocast.member.controller;

import com.neurocast.common.core.domain.Result;
import com.neurocast.member.domain.dto.MemberLoginDto;
import com.neurocast.member.domain.dto.MemberRegisterDto;
import com.neurocast.member.domain.vo.MemberLoginVo;
import com.neurocast.member.service.AppAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员认证接口（C 端移动端用户）
 */
@RestController
@RequestMapping("/api/app/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final AppAuthService appAuthService;

    /**
     * 手机号 + 密码登录
     */
    @PostMapping("/login")
    public Result<MemberLoginVo> login(@Valid @RequestBody MemberLoginDto loginDto) {
        return Result.ok(appAuthService.login(loginDto));
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<MemberLoginVo> register(@Valid @RequestBody MemberRegisterDto registerDto) {
        return Result.ok(appAuthService.register(registerDto));
    }
}
