package com.neurocast.framework.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 接口访问日志过滤器：在入口统一记录 方法/URI/请求参数/耗时/请求体。
 * <p>
 * 使用 Spring 自带的 {@link ContentCachingRequestWrapper} 缓存请求体，
 * 入口记录 method/URI/params，出口记录 status/cost/body。
 * <ul>
 *   <li>JSON 请求体中的敏感字段（密码/令牌/密钥）自动脱敏</li>
 *   <li>文件上传下载等二进制接口不记录请求体</li>
 * </ul>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessLogFilter extends OncePerRequestFilter {

    /**
     * 请求体最大记录长度，超出截断，防止大报文撑爆日志
     */
    private static final int MAX_BODY_LOG_LENGTH = 2048;

    /**
     * 敏感字段脱敏：匹配 "password":"xxx" 形式的 JSON 键值对
     */
    private static final Pattern SENSITIVE_JSON_PATTERN = Pattern.compile(
            "\"(password|token|refreshToken|accessToken|apiKey|secret)\"\\s*:\\s*\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);

    /**
     * 不记录请求体的路径前缀（二进制流）
     */
    private static final String[] BODY_SKIP_PREFIXES = {
            "/api/file/download",
            "/api/file/upload/chunk",
            "/api/file/upload/batch"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String params = sanitize(request.getQueryString());
        boolean logBody = shouldLogBody(request);

        // 包装请求：Spring ContentCachingRequestWrapper 在下游消费 InputStream 时自动缓存字节
        ContentCachingRequestWrapper wrappedRequest = logBody
                ? new ContentCachingRequestWrapper(request) : null;

        // 入口日志：method + URI + params
        log.info("[ACCESS] → {} {} params={}", method, uri, params);

        try {
            filterChain.doFilter(wrappedRequest != null ? wrappedRequest : request, response);
        } finally {
            // 出口日志：status + cost + body（此时 Controller 已消费请求流，body 已缓存）
            long cost = System.currentTimeMillis() - start;
            if (logBody) {
                String body = getBody(wrappedRequest);
                log.info("[ACCESS] ← {} {} status={} cost={}ms body={}", method, uri, response.getStatus(), cost, body);
            } else {
                log.info("[ACCESS] ← {} {} status={} cost={}ms", method, uri, response.getStatus(), cost);
            }
        }
    }

    /**
     * 仅对 JSON 请求体记录 body，且排除二进制流接口
     */
    private boolean shouldLogBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE)) {
            return false;
        }
        if (HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        for (String prefix : BODY_SKIP_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从 ContentCachingRequestWrapper 获取已缓存的请求体（脱敏 + 截断）
     */
    private String getBody(ContentCachingRequestWrapper wrapped) {
        if (wrapped == null) {
            return "-";
        }
        byte[] buf = wrapped.getContentAsByteArray();
        if (buf.length == 0) {
            return "-";
        }
        String body = new String(buf, StandardCharsets.UTF_8);
        if (body.length() > MAX_BODY_LOG_LENGTH) {
            body = body.substring(0, MAX_BODY_LOG_LENGTH) + "...(truncated)";
        }
        return SENSITIVE_JSON_PATTERN.matcher(body).replaceAll("\"$1\":\"***\"");
    }

    private String sanitize(String queryString) {
        return StringUtils.hasText(queryString) ? queryString : "-";
    }
}
