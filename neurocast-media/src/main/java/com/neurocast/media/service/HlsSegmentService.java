package com.neurocast.media.service;

import java.nio.file.Path;

import com.neurocast.media.domain.CameraVideo;

/**
 * HLS 分片服务。
 * 接收上传完成的视频文件，用 FFmpeg 分片为 TS，生成 manifest 和 m3u8。
 */
public interface HlsSegmentService {

    /**
     * 异步处理上传完成的视频文件：FFmpeg 分片 + 生成 manifest
     *
     * @param cameraVideo 录像记录实体（需包含 startTime、duration）
     */
    void processFileAsync(CameraVideo cameraVideo);

    /**
     * 获取单个文件缓存目录的 m3u8 内容
     *
     * @param deviceUid     设备 Uid
     * @param fileStartTime 文件起始时间（秒级时间戳）
     * @param fileEndTime   文件结束时间（秒级时间戳）
     * @return m3u8 文本内容，缓存目录不存在时返回 null
     */
    String getFileM3u8Content(String deviceUid, Long fileStartTime, Long fileEndTime);

    /**
     * 获取 TS 分片文件的磁盘路径
     */
    Path getSegmentPath(String deviceUid, Long fileStartTime, Long fileEndTime, String segmentName);
}
