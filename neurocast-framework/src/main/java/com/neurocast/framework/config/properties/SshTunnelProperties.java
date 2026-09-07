package com.neurocast.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SSH 隧道配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neurocast.ssh-tunnel")
public class SshTunnelProperties {

    /**
     * SSH 服务器地址
     */
    private String serverAddr;

    /**
     * SSH 服务器端口
     */
    private Integer serverPort = 22;

    /**
     * SSH 用户名
     */
    private String username;

    /**
     * SSH 密钥（私钥内容或密钥文件路径）
     */
    private String privateKey;

    /**
     * SSH 密码（与 privateKey 二选一）
     */
    private String password;

    /**
     * 端口分配服务地址（用于申请空闲远程端口）
     */
    private String portServiceUrl;
}
