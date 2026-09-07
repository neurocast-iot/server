package com.neurocast.framework.security;

import com.alibaba.fastjson2.JSON;
import com.neurocast.common.core.constant.RedisKeys;
import com.neurocast.common.utils.IdUtils;
import com.neurocast.framework.config.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 管理后台 Token 服务：生成不透明令牌，存于 Redis，支持即时失效。
 * <p>
 * 与 JWT 不同，Redis Token 可以在服务端主动作废（登出、角色变更、禁用用户等场景）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    /**
     * 生成访问令牌并将用户信息存入 Redis
     *
     * @return 不透明令牌字符串
     */
    public String createAccessToken(LoginUser loginUser) {
        String token = IdUtils.simpleUuid();
        String key = RedisKeys.AdminToken.ACCESS_TOKEN + token;
        long timeoutSeconds = jwtProperties.getAccessTokenExpireSeconds();
        redisTemplate.opsForValue().set(key, JSON.toJSONString(loginUser), timeoutSeconds, TimeUnit.SECONDS);
        return token;
    }

    /**
     * 根据令牌加载用户信息，令牌无效或已过期时返回 null
     */
    public LoginUser loadUser(String token) {
        String key = RedisKeys.AdminToken.ACCESS_TOKEN + token;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        String json = value instanceof String s ? s : JSON.toJSONString(value);
        return JSON.parseObject(json, LoginUser.class);
    }

    /**
     * 删除令牌（登出 / 强制失效）
     */
    public void removeToken(String token) {
        redisTemplate.delete(RedisKeys.AdminToken.ACCESS_TOKEN + token);
    }

    /**
     * 删除用户的所有令牌（角色变更 / 禁用时调用）。
     * <p>
     * 当前设计为单 token 模型（同一用户仅保留一份），
     * 通过 REFRESH_TOKEN key 关联 userId 来定位 access token。
     */
    public void removeUserTokens(Long userId) {
        redisTemplate.delete(RedisKeys.Auth.REFRESH_TOKEN + userId);
    }
}
