package com.neurocast.framework.security;

import java.io.IOException;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.neurocast.common.core.constant.RedisKeys;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 认证过滤器：解析 Authorization 头中的访问令牌并建立认证上下文。
 * 令牌无效/过期/已登出时不建立认证，由 Security 统一返回 401。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(jwtService.getHeader());
        if (StringUtils.hasText(header) && header.startsWith(jwtService.getTokenPrefix())
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(jwtService.getTokenPrefix().length()).trim();
            try {
                Claims claims = jwtService.parseToken(token);
                // 登出黑名单检查
                Boolean loggedOut = redisTemplate.hasKey(RedisKeys.Auth.LOGOUT_TOKEN + claims.getId());
                if (!Boolean.TRUE.equals(loggedOut)) {
                    LoginUser loginUser = LoginUser.ofUser(
                            jwtService.getUserId(claims),
                            jwtService.getUsername(claims),
                            jwtService.getRoleCode(claims),
                            java.util.Set.of());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("JWT 令牌无效：{}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
