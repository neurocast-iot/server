package com.neurocast.system.service.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.RedisKeys;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.framework.config.properties.JwtProperties;
import com.neurocast.framework.security.JwtService;
import com.neurocast.framework.security.LoginUser;
import com.neurocast.framework.security.SecurityUtils;
import com.neurocast.framework.security.TokenService;
import com.neurocast.system.constant.RoleCode;
import com.neurocast.system.domain.SysUser;
import com.neurocast.system.domain.dto.LoginDto;
import com.neurocast.system.domain.vo.LoginVo;
import com.neurocast.system.domain.vo.SysUserVo;
import com.neurocast.system.mapper.SysPermissionMapper;
import com.neurocast.system.mapper.SysRoleMapper;
import com.neurocast.system.mapper.SysUserMapper;
import com.neurocast.system.service.AuthService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Integer STATUS_ENABLED = 1;

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenService tokenService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginVo login(LoginDto loginDto) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDto.username()));
        if (user == null || !passwordEncoder.matches(loginDto.password(), user.getPassword())) {
            throw new ServiceException(ApiStatus.LOGIN_USERNAME_PASSWORD_ERROR);
        }
        if (!STATUS_ENABLED.equals(user.getStatus())) {
            throw new ServiceException(ApiStatus.LOGIN_USER_DISABLED);
        }

        String roleCode = firstRoleCode(user.getId());
        Set<String> permissions = loadPermissions(user.getId());

        // 生成 Redis 访问令牌
        LoginUser loginUser = LoginUser.ofUser(user.getId(), user.getUsername(), roleCode, permissions);
        String accessToken = tokenService.createAccessToken(loginUser);

        // 刷新令牌存 Redis，登录时覆盖旧令牌（同一用户仅保留一份）
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        redisTemplate.opsForValue().set(RedisKeys.Auth.REFRESH_TOKEN + user.getId(), refreshToken,
                jwtProperties.getRefreshTokenExpireDays(), TimeUnit.DAYS);

        log.info("用户登录成功：{}", user.getUsername());
        return LoginVo.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpireSeconds(),
                user.getId(), user.getUsername(), user.getNickname(), roleCode);
    }

    @Override
    public LoginVo refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ServiceException(ApiStatus.REFRESH_TOKEN_INVALID);
        }

        Long userId = jwtService.getUserId(claims);
        Object cached = redisTemplate.opsForValue().get(RedisKeys.Auth.REFRESH_TOKEN + userId);
        if (cached == null || !refreshToken.equals(cached.toString())) {
            throw new ServiceException(ApiStatus.REFRESH_TOKEN_INCORRECT);
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !STATUS_ENABLED.equals(user.getStatus())) {
            throw new ServiceException(ApiStatus.LOGIN_USER_DISABLED);
        }

        String roleCode = firstRoleCode(user.getId());
        Set<String> permissions = loadPermissions(user.getId());

        // 生成新的 Redis 访问令牌
        LoginUser loginUser = LoginUser.ofUser(user.getId(), user.getUsername(), roleCode, permissions);
        String accessToken = tokenService.createAccessToken(loginUser);

        return LoginVo.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpireSeconds(),
                user.getId(), user.getUsername(), user.getNickname(), roleCode);
    }

    @Override
    public void logout(HttpServletRequest request) {
        String header = request.getHeader(jwtService.getHeader());
        if (!StringUtils.hasText(header) || !header.startsWith(jwtService.getTokenPrefix())) {
            return;
        }
        String token = header.substring(jwtService.getTokenPrefix().length()).trim();
        // 删除 Redis 中的访问令牌
        tokenService.removeToken(token);
        log.info("用户登出：token已失效");
    }

    @Override
    public SysUserVo getUserInfo() {
        SysUser user = sysUserMapper.selectById(SecurityUtils.getUserId());
        if (user == null) {
            throw new ServiceException(ApiStatus.BUSINESS_USER_NOT_EXISTED);
        }
        return SysUserVo.from(user);
    }

    @Override
    public List<String> getUserPermissions() {
        return sysPermissionMapper.selectPermissionCodesByUserId(SecurityUtils.getUserId());
    }

    /**
     * 用户主角色编码，无角色时默认 VIEWER
     */
    private String firstRoleCode(Long userId) {
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(userId);
        return roleCodes.isEmpty() ? RoleCode.VIEWER : roleCodes.get(0);
    }

    /**
     * 加载用户通过角色关联的权限标识集合
     */
    private Set<String> loadPermissions(Long userId) {
        return Set.copyOf(sysPermissionMapper.selectPermissionCodesByUserId(userId));
    }
}
