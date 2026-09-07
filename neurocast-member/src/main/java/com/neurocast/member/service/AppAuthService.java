package com.neurocast.member.service;

import com.neurocast.member.domain.MemberUser;
import com.neurocast.member.domain.dto.MemberLoginDto;
import com.neurocast.member.domain.dto.MemberRegisterDto;
import com.neurocast.member.domain.dto.MemberUpdateProfileDto;
import com.neurocast.member.domain.vo.MemberLoginVo;

/**
 * 会员认证服务
 */
public interface AppAuthService {

    /**
     * 手机号 + 密码登录
     */
    MemberLoginVo login(MemberLoginDto loginDto);

    /**
     * 注册（手机号 + 密码）
     */
    MemberLoginVo register(MemberRegisterDto registerDto);

    /**
     * 更新个人资料
     */
    void updateProfile(String userId, MemberUpdateProfileDto dto);

    /**
     * 获取当前用户信息
     */
    MemberUser getUserInfo(String userId);
}
