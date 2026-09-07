package com.neurocast.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HLS 视频回放配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neurocast.hls")
public class HlsProperties {

    /**
     * FFmpeg 可执行文件路径，默认从 PATH 中查找
     */
    private String ffmpegPath = "ffmpeg";

    /**
     * FFprobe 可执行文件路径，默认从 PATH 中查找
     */
    private String ffprobePath = "ffprobe";

    /**
     * 单个 TS 分片目标时长（秒）
     */
    private int targetSegmentDuration = 10;

    /**
     * HLS 缓存保留时长（小时），过期后自动清理
     */
    private int cacheRetentionHours = 24;
}
