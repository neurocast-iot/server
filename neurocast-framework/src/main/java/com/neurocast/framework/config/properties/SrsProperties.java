package com.neurocast.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SRS 流媒体服务器配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neurocast.srs")
public class SrsProperties {

    /**
     * SRS HTTP API 地址，如 http://192.168.1.10:1985
     */
    private String apiUrl;

    /**
     * 播放基础地址（对外），如 http://192.168.1.10:8080/
     */
    private String baseUrl;

    /**
     * 实时流推流地址模板，支持占位符 {deviceUid}、{accessToken}
     * 如 rtmp://192.168.1.10:1935/live/{deviceUid}?accessToken={accessToken}
     */
    private String pushCameraStreamUrl;
}
