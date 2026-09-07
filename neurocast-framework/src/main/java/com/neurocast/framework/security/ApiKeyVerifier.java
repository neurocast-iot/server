package com.neurocast.framework.security;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * API Key 凭证查询端口：认证过滤器不感知凭证的存储方式（数据库/配置中心等），
 * 由业务模块提供实现
 */
public interface ApiKeyVerifier {

    /**
     * 根据 API Key 加载客户端凭证，不存在返回 null
     */
    ApiClientCredential verify(String apiKey);

    /**
     * API 客户端凭证（认证所需的最小信息集）
     */
    record ApiClientCredential(Long clientId, String clientCode, Set<String> scopes,
                               Integer status, OffsetDateTime expireTime) {

        private static final Integer STATUS_ENABLED = 1;

        /**
         * 启用且未过期
         */
        public boolean isActive() {
            return STATUS_ENABLED.equals(status)
                    && (expireTime == null || expireTime.isAfter(OffsetDateTime.now()));
        }
    }
}
