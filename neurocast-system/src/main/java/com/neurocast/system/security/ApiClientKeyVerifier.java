package com.neurocast.system.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neurocast.common.core.constant.RedisKeys;
import com.neurocast.framework.config.properties.MediaFileProperties;
import com.neurocast.framework.security.ApiKeyVerifier;
import com.neurocast.system.domain.ApiClient;
import com.neurocast.system.domain.ApiClientScope;
import com.neurocast.system.mapper.ApiClientMapper;
import com.neurocast.system.mapper.ApiClientScopeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * {@link ApiKeyVerifier} 实现：API 客户端凭证存于 api_client 表，
 * 校验结果缓存于 Redis（neurocast:api:client:{apiKey}，30 分钟）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiClientKeyVerifier implements ApiKeyVerifier {

    private final ApiClientMapper apiClientMapper;
    private final ApiClientScopeMapper apiClientScopeMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MediaFileProperties mediaFileProperties;

    @Override
    public ApiClientCredential verify(String apiKey) {
        // 优先匹配设备上传固定 API Key
        if (isDeviceUploadKey(apiKey)) {
            log.debug("设备上传 API Key 认证通过");
            return new ApiClientCredential(0L, "device-upload", Collections.emptySet(),
                    1, null);
        }

        ApiClient apiClient = loadApiClient(apiKey);
        if (apiClient == null) {
            return null;
        }
        Set<String> scopes = loadScopes(apiClient.getId());
        return new ApiClientCredential(apiClient.getId(), apiClient.getClientCode(),
                scopes, apiClient.getStatus(), apiClient.getExpireTime());
    }

    /**
     * 判断是否为配置文件中的设备上传 API Key
     */
    private boolean isDeviceUploadKey(String apiKey) {
        String deviceKey = mediaFileProperties.getUploadFileApiKey();
        return StringUtils.hasText(deviceKey) && deviceKey.equals(apiKey);
    }

    /**
     * 加载客户端的权限范围列表
     */
    private Set<String> loadScopes(Long clientId) {
        return apiClientScopeMapper.selectList(new LambdaQueryWrapper<ApiClientScope>()
                        .eq(ApiClientScope::getClientId, clientId))
                .stream()
                .map(ApiClientScope::getScope)
                .collect(Collectors.toSet());
    }

    /**
     * 先查 Redis 缓存，未命中再查库并回填缓存
     */
    private ApiClient loadApiClient(String apiKey) {
        String cacheKey = RedisKeys.ApiClient.API_KEY + apiKey;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof ApiClient apiClient) {
                return apiClient;
            }
        } catch (Exception e) {
            log.warn("读取 API 客户端缓存失败，降级查库", e);
        }

        ApiClient apiClient = apiClientMapper.selectOne(new LambdaQueryWrapper<ApiClient>()
                .eq(ApiClient::getApiKey, apiKey));
        if (apiClient != null) {
            redisTemplate.opsForValue().set(cacheKey, apiClient,
                    RedisKeys.ApiClient.API_KEY_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        }
        return apiClient;
    }
}
