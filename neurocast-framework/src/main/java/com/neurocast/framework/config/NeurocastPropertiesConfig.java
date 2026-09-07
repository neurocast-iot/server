package com.neurocast.framework.config;

import com.neurocast.framework.config.properties.FrpProperties;
import com.neurocast.framework.config.properties.FtpProperties;
import com.neurocast.framework.config.properties.HlsProperties;
import com.neurocast.framework.config.properties.JwtProperties;
import com.neurocast.framework.config.properties.MediaFileProperties;
import com.neurocast.framework.config.properties.SrsProperties;
import com.neurocast.framework.config.properties.ThingsBoardProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 统一启用类型化配置属性。
 * 所有第三方服务/业务参数一律通过 neurocast.* 前缀的配置项注入，
 * 禁止在业务代码中直接使用 @Value 读取配置。
 */
@Configuration
@EnableConfigurationProperties({
        ThingsBoardProperties.class,
        SrsProperties.class,
        FtpProperties.class,
        FrpProperties.class,
        MediaFileProperties.class,
        JwtProperties.class,
        HlsProperties.class
})
public class NeurocastPropertiesConfig {
}
