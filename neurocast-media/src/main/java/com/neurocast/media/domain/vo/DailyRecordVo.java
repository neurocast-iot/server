package com.neurocast.media.domain.vo;

import com.neurocast.common.core.constant.HlsStatus;
import com.neurocast.media.domain.CameraVideo;

/**
 * 每日可播放录像记录项（仅包含可播放文件的核心信息）
 */
public record DailyRecordVo(
        /** 文件 ID */
        String id,
        /** 文件名 */
        String name,
        /** 录像起始时间（秒级时间戳） */
        Long startTime,
        /** 录像时长（秒） */
        Integer duration,
        /** HLS 分片状态 */
        String hlsStatus
) {

    public static DailyRecordVo from(CameraVideo video) {
        return new DailyRecordVo(
                video.getId(),
                video.getName(),
                video.getStartTime(),
                video.getDuration(),
                mapHlsStatus(video.getHlsStatus())
        );
    }

    private static String mapHlsStatus(Integer hlsStatus) {
        if (hlsStatus == null) {
            return "pending";
        }
        return switch (hlsStatus) {
            case HlsStatus.PENDING -> "pending";
            case HlsStatus.PROCESSING -> "processing";
            case HlsStatus.READY -> "ready";
            case HlsStatus.FAILED -> "failed";
            default -> "pending";
        };
    }
}
