package com.neurocast.framework.security;

import com.alibaba.fastjson2.JSON;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.domain.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
/**
 * Spring Security 配置：
 * - 无状态会话，JWT + API Key 双通道认证
 * - 登录/刷新令牌/SRS 回调/文件下载（内部凭证校验）放行，其余接口需认证
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    /**
     * 放行路径：登录、刷新令牌、文件下载（内部用临时 accessToken 校验）、
     * HLS 播放（内部用流 accessToken 校验）
     */
    private static final String[] PERMIT_ALL_PATHS = {
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/app/auth/login",
            "/api/app/auth/register",
            "/api/file/download/**",
            "/api/admin/media/hls/**",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, e) ->
                                writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, Result.failed(ApiStatus.UNAUTHORIZED)))
                        .accessDeniedHandler((request, response, e) ->
                                writeResult(response, HttpServletResponse.SC_FORBIDDEN, Result.failed(ApiStatus.FORBIDDEN))))
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 权限校验服务 Bean，供 @PreAuthorize("@ss.hasPermission(...)") 使用
     */
    @Bean("ss")
    public PermissionService permissionService() {
        return new PermissionService();
    }

    private void writeResult(HttpServletResponse response, int httpStatus, Result<Void> result) throws java.io.IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSON.toJSONString(result));
    }
}
