package com.neurocast.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FRP 内网穿透配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neurocast.frp")
public class FrpProperties {

    /**
     * frps 服务器地址
     */
    private String serverAddr;

    /**
     * frps 服务器端口
     */
    private Integer serverPort;

    /**
     * 鉴权方式，如 token
     */
    private String method;

    /**
     * 鉴权令牌
     */
    private String token;

    /**
     * 端口分配服务地址（用于申请空闲远程端口）
     */
    private String portServiceUrl;
}
