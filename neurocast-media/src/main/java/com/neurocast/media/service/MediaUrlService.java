package com.neurocast.media.service;

import com.neurocast.framework.config.properties.MediaFileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 媒体下载链接生成器：拼接对外下载地址
 */
@Component
@RequiredArgsConstructor
public class MediaUrlService {

    private final MediaFileProperties mediaFileProperties;

    /**
     * 生成下载链接
     *
     * @param fileType  目录类型：images / videos
     * @param deviceUid 设备 Uid
     * @param name      文件名
     */
    public String genDownloadUrl(String fileType, String deviceUid, String name) {
        return mediaFileProperties.getDownloadBaseUrl() + "/" + fileType + "/" + deviceUid + "/" + name;
    }
}
