package com.neurocast.member.controller;

import com.neurocast.common.core.domain.Result;
import com.neurocast.framework.security.SecurityUtils;
import com.neurocast.member.domain.MemberUser;
import com.neurocast.member.domain.dto.MemberUpdateProfileDto;
import com.neurocast.member.service.AppAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员资料接口（C 端移动端用户）
 */
@RestController
@RequestMapping("/api/app/user")
@RequiredArgsConstructor
public class AppUserController {

    private final AppAuthService appAuthService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<MemberUser> profile() {
        return Result.ok(appAuthService.getUserInfo(SecurityUtils.getUserId().toString()));
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody MemberUpdateProfileDto dto) {
        appAuthService.updateProfile(SecurityUtils.getUserId().toString(), dto);
        return Result.ok();
    }
}
