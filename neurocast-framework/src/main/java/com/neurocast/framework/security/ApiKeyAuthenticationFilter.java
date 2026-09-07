package com.neurocast.framework.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API Key 认证过滤器：内部系统与设备通过 X-API-KEY 请求头接入，
 * 同时支持 ?apiKey= 查询参数（用于 SRS 等无法自定义请求头的 HTTP 回调场景）。
 * 凭证查询委托给 {@link ApiKeyVerifier} 端口（业务模块实现，含缓存策略）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_API_KEY = "X-API-KEY";

    private final ApiKeyVerifier apiKeyVerifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER_API_KEY);
        if (!StringUtils.hasText(apiKey)) {
            apiKey = request.getParameter("apiKey");
        }
        if (StringUtils.hasText(apiKey)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            ApiKeyVerifier.ApiClientCredential credential = apiKeyVerifier.verify(apiKey);
            if (credential != null && credential.isActive()) {
                LoginUser loginUser = LoginUser.ofApiClient(
                        credential.clientId(), credential.clientCode(), credential.scopes());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (credential != null) {
                log.warn("API 客户端已禁用或已过期：{}", credential.clientCode());
            }
        }
        filterChain.doFilter(request, response);
    }
}
