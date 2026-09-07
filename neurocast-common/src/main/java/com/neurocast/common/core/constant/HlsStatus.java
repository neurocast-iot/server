package com.neurocast.common.core.constant;

/**
 * HLS 分片状态
 */
public interface HlsStatus {

    /**
     * 待处理（尚未开始分片）
     */
    int PENDING = 0;

    /**
     * 分片中（FFmpeg 正在执行）
     */
    int PROCESSING = 1;

    /**
     * 分片完成（TS + manifest + m3u8 均已生成）
     */
    int READY = 2;

    /**
     * 分片失败
     */
    int FAILED = 3;
}
