package com.neurocast.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FTP 文件服务器配置（下发给设备的系统配置）
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neurocast.ftp")
public class FtpProperties {

    private String host;

    private Integer port;

    private String username;

    private String password;

    /**
     * FTP 服务器上的文件存储路径
     */
    private String filesPath;
}
