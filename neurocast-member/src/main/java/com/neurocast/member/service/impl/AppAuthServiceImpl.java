package com.neurocast.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.framework.config.properties.JwtProperties;
import com.neurocast.framework.security.JwtService;
import com.neurocast.member.domain.MemberUser;
import com.neurocast.member.domain.dto.MemberLoginDto;
import com.neurocast.member.domain.dto.MemberRegisterDto;
import com.neurocast.member.domain.dto.MemberUpdateProfileDto;
import com.neurocast.member.domain.vo.MemberLoginVo;
import com.neurocast.member.mapper.MemberUserMapper;
import com.neurocast.member.service.AppAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 会员认证服务实现：使用 JWT 令牌（C 端移动端用户）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements AppAuthService {

    private static final Integer STATUS_ENABLED = 1;

    private final MemberUserMapper memberUserMapper;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberLoginVo login(MemberLoginDto loginDto) {
        MemberUser user = memberUserMapper.selectOne(new LambdaQueryWrapper<MemberUser>()
                .eq(MemberUser::getPhone, loginDto.getPhone()));
        if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new ServiceException(ApiStatus.LOGIN_USERNAME_PASSWORD_ERROR);
        }
        if (!STATUS_ENABLED.equals(user.getStatus())) {
            throw new ServiceException(ApiStatus.LOGIN_USER_DISABLED);
        }
        return buildLoginVo(user);
    }

    @Override
    public MemberLoginVo register(MemberRegisterDto registerDto) {
        Long count = memberUserMapper.selectCount(new LambdaQueryWrapper<MemberUser>()
                .eq(MemberUser::getPhone, registerDto.getPhone()));
        if (count > 0) {
            throw new ServiceException(ApiStatus.DUPLICATE_KEY, "手机号已注册");
        }

        MemberUser user = new MemberUser();
        user.setPhone(registerDto.getPhone());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setNickname(StringUtils.hasText(registerDto.getNickname())
                ? registerDto.getNickname() : "用户" + registerDto.getPhone().substring(7));
        user.setStatus(STATUS_ENABLED);
        memberUserMapper.insert(user);

        log.info("会员注册成功：phone={}", registerDto.getPhone());
        return buildLoginVo(user);
    }

    @Override
    public void updateProfile(String userId, MemberUpdateProfileDto dto) {
        MemberUser user = memberUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ApiStatus.BUSINESS_USER_NOT_EXISTED);
        }
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }
        memberUserMapper.updateById(user);
    }

    @Override
    public MemberUser getUserInfo(String userId) {
        MemberUser user = memberUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ApiStatus.BUSINESS_USER_NOT_EXISTED);
        }
        return user;
    }

    private MemberLoginVo buildLoginVo(MemberUser user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getPhone(), null);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        MemberLoginVo vo = new MemberLoginVo();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtProperties.getAccessTokenExpireSeconds());
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        return vo;
    }
}
