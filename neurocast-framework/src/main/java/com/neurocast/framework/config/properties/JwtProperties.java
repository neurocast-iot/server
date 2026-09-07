package com.neurocast.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neurocast.jwt")
public class JwtProperties {

    /**
     * 签名密钥（HS256 要求至少 32 字节）
     */
    private String secret = "neurocast-server-default-secret-key-please-change-it!";

    /**
     * 请求头名称
     */
    private String header = "Authorization";

    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 访问令牌有效期（秒），默认 2 小时
     */
    private Long accessTokenExpireSeconds = 7200L;

    /**
     * 刷新令牌有效期（天），默认 7 天
     */
    private Long refreshTokenExpireDays = 7L;
}
