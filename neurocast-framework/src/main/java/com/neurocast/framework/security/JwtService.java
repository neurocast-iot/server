package com.neurocast.framework.security;

import com.neurocast.common.utils.IdUtils;
import com.neurocast.framework.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌服务：生成与解析访问令牌、刷新令牌
 */
@Component
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";

    private final JwtProperties jwtProperties;

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username, String roleCode) {
        return generateAccessToken(String.valueOf(userId), username, roleCode);
    }

    /**
     * 生成访问令牌（String userId，支持会员 UUID）
     */
    public String generateAccessToken(String userId, String username, String roleCode) {
        Date now = new Date();
        return Jwts.builder()
                .id(IdUtils.simpleUuid())
                .subject(userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, roleCode)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getAccessTokenExpireSeconds() * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId) {
        return generateRefreshToken(String.valueOf(userId));
    }

    /**
     * 生成刷新令牌（String userId）
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
                .id(IdUtils.simpleUuid())
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getRefreshTokenExpireDays() * 24 * 3600 * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析令牌，无效或过期时抛出 JwtException
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Claims 中获取用户 ID
     */
    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从 Claims 中获取用户名
     */
    public String getUsername(Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    /**
     * 从 Claims 中获取角色编码
     */
    public String getRoleCode(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }

    /**
     * 获取请求头名称
     */
    public String getHeader() {
        return jwtProperties.getHeader();
    }

    /**
     * 获取令牌前缀
     */
    public String getTokenPrefix() {
        return jwtProperties.getTokenPrefix();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
