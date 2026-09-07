package com.neurocast.media.domain.vo;

/**
 * 回放状态视图
 */
public record PlaybackStatusVo(
        /** m3u8 播放地址 */
        String m3u8Url,
        /** preparing / partial / ready */
        String status,
        /** 总文件数 */
        Integer totalFiles,
        /** 已就绪文件数 */
        Integer readyFiles,
        /** 已覆盖时长（秒） */
        Double coveredDuration,
        /** 请求总时长（秒） */
        Double totalDuration
) {}
