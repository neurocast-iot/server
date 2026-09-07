package com.neurocast.media.domain.vo;

import com.neurocast.common.core.constant.FileStatus;
import com.neurocast.common.core.constant.HlsStatus;
import com.neurocast.media.domain.CameraVideo;

/**
 * 回放文件列表项。
 * status 为上传状态（pending / uploading / ready / failed），
 * hlsStatus 为 HLS 分片状态（pending / processing / ready / failed）。
 */
public record PlaybackFileVo(
        /** 文件 ID */
        String id,
        /** 文件名 */
        String name,
        /** 文件数据实际起始时间（秒级时间戳） */
        Long startTime,
        /** 录像时长（秒） */
        Integer duration,
        /** 上传语义状态：pending / uploading / ready / failed */
        String status,
        /** HLS 分片状态：pending / processing / ready / failed */
        String hlsStatus,
        /** HLS 分片是否已就绪 */
        boolean hlsReady,
        /** m3u8 播放地址（仅 hlsReady=true 时返回） */
        String m3u8Url
) {

    /**
     * 从 CameraVideo 实体转换，需额外传入 hlsReady 标志（由 Service 层判定）。
     */
    public static PlaybackFileVo from(CameraVideo video, boolean hlsReady) {
        return new PlaybackFileVo(
                video.getId(),
                video.getName(),
                video.getStartTime(),
                video.getDuration(),
                mapStatus(video.getStatus()),
                mapHlsStatus(video.getHlsStatus()),
                hlsReady,
                null
        );
    }

    /**
     * 从 CameraVideo 实体转换，hlsReady 时附带 m3u8Url
     */
    public static PlaybackFileVo from(CameraVideo video, boolean hlsReady, String m3u8Url) {
        return new PlaybackFileVo(
                video.getId(),
                video.getName(),
                video.getStartTime(),
                video.getDuration(),
                mapStatus(video.getStatus()),
                mapHlsStatus(video.getHlsStatus()),
                hlsReady,
                m3u8Url
        );
    }

    /**
     * 将上传状态整数码映射为语义字符串
     */
    private static String mapStatus(Integer status) {
        if (status == null) {
            return "pending";
        }
        return switch (status) {
            case FileStatus.INIT -> "pending";
            case FileStatus.PREPARING -> "uploading";
            case FileStatus.PREPARE_SUCCESS -> "ready";
            case FileStatus.PREPARE_FAILED -> "failed";
            default -> "pending";
        };
    }

    /**
     * 将 HLS 分片状态整数码映射为语义字符串
     */
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
