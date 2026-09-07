package com.neurocast.framework.config;

import com.neurocast.framework.config.properties.SrsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * SRS 客户端配置
 */
@Configuration
public class SrsConfig {

    /**
     * SRS HTTP API 专用 RestClient，baseUrl 为 neurocast.srs.api-url
     */
    @Bean
    public RestClient srsRestClient(SrsProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .build();
    }
}
