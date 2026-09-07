package com.neurocast.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 媒体文件配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neurocast.file")
public class MediaFileProperties {

    /**
     * 本地文件存储根目录，如 ./files
     */
    private String basePath = "./files";

    /**
     * 文件下载基础地址（对外），如 http://192.168.1.10:8189/api/v1/camera/file/download
     */
    private String downloadBaseUrl;

    /**
     * 抓拍图片保留天数
     */
    private Integer imageRetentionDays = 7;

    /**
     * 录像记录保留天数
     */
    private Integer videoRetentionDays = 30;

    /**
     * 单文件上传大小上限（字节）
     */
    private Long uploadMaxSize = 104857600L;

    /**
     * 分片上传任务过期时间（小时）
     */
    private Integer chunkExpireHours = 24;

    /**
     * 外部文件上传服务地址（OTA 包等）
     */
    private String uploadFileBaseUrl;

    /**
     * 外部文件上传服务 API Key
     */
    private String uploadFileApiKey;
}
