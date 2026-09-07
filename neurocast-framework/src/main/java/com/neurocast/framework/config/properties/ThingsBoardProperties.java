package com.neurocast.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ThingsBoard 集成配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neurocast.thingsboard")
public class ThingsBoardProperties {

    /**
     * ThingsBoard REST 服务地址，如 http://thingsboard:9090/
     */
    private String url;

    /**
     * 租户账号
     */
    private String username;

    /**
     * 租户密码
     */
    private String password;

    /**
     * 摄像头设备 Profile ID
     */
    private String deviceProfileId;
}
