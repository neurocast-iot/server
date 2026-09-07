package com.neurocast.framework.config;

import com.neurocast.framework.config.properties.ThingsBoardProperties;
import com.neurocast.framework.integration.thingsboard.ThingsBoardClientManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ThingsBoard 客户端配置
 */
@Configuration
public class ThingsBoardConfig {

    @Bean
    public ThingsBoardClientManager thingsBoardClientManager(ThingsBoardProperties properties) {
        return new ThingsBoardClientManager(properties);
    }
}
