package com.neurocast.media.domain.vo;

import java.util.List;

/**
 * 文件列表查询结果：包含文件列表和 m3u8 播放地址
 */
public record PlaybackFilesResultVo(
        /** m3u8 播放地址 */
        String m3u8Url,
        /** 文件列表 */
        List<PlaybackFileVo> files,
        /** 播放开始时间（秒级时间戳） */
        Long startTime,
        /** 实际可播放时长（秒） */
        Double duration
) {}
