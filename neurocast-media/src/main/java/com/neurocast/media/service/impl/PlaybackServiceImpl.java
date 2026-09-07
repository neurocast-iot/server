package com.neurocast.media.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.FileStatus;
import com.neurocast.common.core.constant.HlsStatus;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.device.domain.Device;
import com.neurocast.device.protocol.DeviceRpcMethod;
import com.neurocast.device.service.DeviceService;
import com.neurocast.framework.config.properties.HlsProperties;
import com.neurocast.framework.config.properties.MediaFileProperties;
import com.neurocast.framework.integration.thingsboard.RpcBody;
import com.neurocast.framework.integration.thingsboard.ThingsBoardDeviceClient;
import com.neurocast.media.domain.CameraVideo;
import com.neurocast.media.domain.dto.PlaybackPrepareDto;
import com.neurocast.media.domain.vo.DailyRecordVo;
import com.neurocast.media.domain.vo.PlaybackFileVo;
import com.neurocast.media.domain.vo.PlaybackFilesResultVo;
import com.neurocast.media.domain.vo.PlaybackM3u8ResultVo;
import com.neurocast.media.domain.vo.PlaybackStatusVo;
import com.neurocast.media.service.CameraVideoService;
import com.neurocast.media.service.HlsSegmentService;
import com.neurocast.media.service.PlaybackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HLS 回放服务实现。
 * 前端编排模式：查询文件列表 → 逐个触发上传 → 动态 m3u8 累积已就绪分片。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaybackServiceImpl implements PlaybackService {

    private static final String DIR_HLS = "hls";
    private static final String MANIFEST_FILE = "manifest.json";

    private final CameraVideoService cameraVideoService;
    private final HlsSegmentService hlsSegmentService;
    private final ThingsBoardDeviceClient thingsBoardDeviceClient;
    private final DeviceService deviceService;
    private final MediaFileProperties mediaFileProperties;
    private final HlsProperties hlsProperties;

    @Override
    public PlaybackFilesResultVo listFiles(String deviceUid, Long startTime, Long endTime) {
        List<CameraVideo> videos = cameraVideoService.findByTimeRange(deviceUid, startTime, endTime);
        List<PlaybackFileVo> files = new ArrayList<>();
        // 计算理论可播放时长（基于 DB 记录，与文件状态无关）
        double duration = 0;
        for (CameraVideo video : videos) {
            boolean hlsReady = false;
            if (video.getStartTime() != null && video.getDuration() != null
                    && video.getStatus() != null && video.getStatus() == FileStatus.PREPARE_SUCCESS) {
                int hlsStatus = video.getHlsStatus() != null ? video.getHlsStatus() : HlsStatus.PENDING;
                hlsReady = (hlsStatus == HlsStatus.READY);
            }
            files.add(PlaybackFileVo.from(video, hlsReady));
            // 计算文件在请求范围内的覆盖
            if (video.getStartTime() != null && video.getDuration() != null) {
                long fStart = video.getStartTime();
                long fEnd = fStart + video.getDuration();
                long overlapStart = Math.max(fStart, startTime);
                long overlapEnd = Math.min(fEnd, endTime);
                if (overlapEnd > overlapStart) {
                    duration += (overlapEnd - overlapStart);
                }
            }
        }

        String m3u8Url = buildM3u8Url(deviceUid, startTime, endTime);
        return new PlaybackFilesResultVo(m3u8Url, files, startTime, duration);
    }

    @Override
    public PlaybackFileVo prepareFile(PlaybackPrepareDto dto) {
        CameraVideo video = cameraVideoService.findEntityById(dto.fileId());
        if (video == null) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_NOT_EXISTED);
        }

        int status = video.getStatus() != null ? video.getStatus() : FileStatus.INIT;
        int hlsStatus = video.getHlsStatus() != null ? video.getHlsStatus() : HlsStatus.PENDING;

        // 场景 1：已上传成功，检查 HLS 分片
        if (status == FileStatus.PREPARE_SUCCESS) {
            if (video.getStartTime() != null && video.getDuration() != null) {
                long fEnd = video.getStartTime() + video.getDuration();

                // DB 状态为 READY，分片已完成，直接返回 m3u8Url
                if (hlsStatus == HlsStatus.READY) {
                    String m3u8Url = buildM3u8Url(video.getDeviceUid(), video.getStartTime(), fEnd);
                    return PlaybackFileVo.from(video, true, m3u8Url);
                }

                // PROCESSING：分片中，等待下次轮询
                // PENDING 或 FAILED：触发分片
                if (hlsStatus == HlsStatus.PENDING || hlsStatus == HlsStatus.FAILED) {
                    if (hlsStatus == HlsStatus.FAILED) {
                        cameraVideoService.updateHlsStatus(video.getId(), HlsStatus.PENDING);
                    }
                    hlsSegmentService.processFileAsync(video);
                }
            }
            return PlaybackFileVo.from(video, false);
        }

        // 场景 2：正在上传中，直接返回当前状态
        if (status == FileStatus.PREPARING) {
            return PlaybackFileVo.from(video, false);
        }

        // 场景 3：INIT 或 PREPARE_FAILED，触发上传
        triggerSingleUpload(video.getDeviceUid(), video);
        return PlaybackFileVo.from(video, false);
    }

    @Override
    public String getPlaybackM3u8(String deviceUid, Long startTime, Long endTime) {
        PlaybackM3u8ResultVo result = getPlaybackM3u8WithStatus(deviceUid, startTime, endTime);
        return result.m3u8Content();
    }

    @Override
    public PlaybackM3u8ResultVo getPlaybackM3u8WithStatus(String deviceUid, Long startTime, Long endTime) {
        List<CameraVideo> videos = cameraVideoService.findByTimeRange(deviceUid, startTime, endTime);
        int totalFiles = videos.size();
        int readyFiles = 0;
        // 是否所有文件都已就绪（用于决定是否添加 EXT-X-ENDLIST）
        boolean allReady = true;

        int targetDuration = hlsProperties.getTargetSegmentDuration();
        // 先扫描连续就绪分片，计算实际最大分片时长，确保 TARGETDURATION 合规
        int actualMaxDuration = targetDuration;
        List<List<double[]>> fileSegmentsList = new ArrayList<>();
        List<String[]> fileUrlsList = new ArrayList<>();
        for (CameraVideo video : videos) {
            if (video.getStartTime() == null || video.getDuration() == null) {
                // 无时间信息的文件不计入总数
                totalFiles--;
                continue;
            }
            long fStart = video.getStartTime();
            long fEnd = fStart + video.getDuration();
            int hlsStatus = video.getHlsStatus() != null ? video.getHlsStatus() : HlsStatus.PENDING;

            if (hlsStatus != HlsStatus.READY) {
                // 遇到第一个未就绪的文件，停止遍历（不跳过，避免断层）
                // 分片处理由 prepare 接口负责，m3u8 端点保持纯只读
                allReady = false;
                break;
            }
            readyFiles++;

            Path cacheDir = resolveCacheDir(deviceUid, fStart, fEnd);
            Path manifestPath = cacheDir.resolve(MANIFEST_FILE);
            try {
                String content = Files.readString(manifestPath);
                JSONObject manifest = JSONObject.parseObject(content);
                JSONArray segments = manifest.getJSONArray("segments");
                if (segments != null) {
                    List<double[]> segDurations = new ArrayList<>();
                    List<String> segUrls = new ArrayList<>();
                    // 计算文件与请求时间范围的重叠区间
                    long overlapStart = Math.max(fStart, startTime);
                    long overlapEnd = Math.min(fEnd, endTime);
                    // 跟踪每个分片的绝对时间
                    double segOffset = 0;
                    for (int i = 0; i < segments.size(); i++) {
                        JSONObject seg = segments.getJSONObject(i);
                        double duration = seg.getDoubleValue("duration");
                        String filename = seg.getString("filename");
                        // 计算分片的绝对时间
                        double segAbsStart = fStart + segOffset;
                        double segAbsEnd = segAbsStart + duration;
                        // 只包含与请求时间范围重叠的分片
                        if (segAbsStart < overlapEnd && segAbsEnd > overlapStart) {
                            String segUrl = "/api/admin/media/hls/" + deviceUid + "/"
                                    + fStart + "-" + fEnd + "/" + filename;
                            segDurations.add(new double[]{duration});
                            segUrls.add(segUrl);
                            if ((int) Math.ceil(duration) > actualMaxDuration) {
                                actualMaxDuration = (int) Math.ceil(duration);
                            }
                        }
                        segOffset += duration;
                    }
                    if (!segDurations.isEmpty()) {
                        fileSegmentsList.add(segDurations);
                        fileUrlsList.add(segUrls.toArray(new String[0]));
                    }
                }
            } catch (IOException e) {
                log.warn("读取 manifest 失败：{}", manifestPath, e);
            }
        }

        // 构建 m3u8 内容（始终使用 VOD 模式，避免 hls.js 频繁轮询）
        StringBuilder m3u8 = new StringBuilder();
        m3u8.append("#EXTM3U\n");
        m3u8.append("#EXT-X-VERSION:3\n");
        m3u8.append("#EXT-X-TARGETDURATION:").append(actualMaxDuration).append("\n");
        m3u8.append("#EXT-X-MEDIA-SEQUENCE:0\n");
        m3u8.append("#EXT-X-PLAYLIST-TYPE:EVENT\n");

        boolean hasSegments = false;
        boolean firstFile = true;
        for (int f = 0; f < fileSegmentsList.size(); f++) {
            List<double[]> segDurations = fileSegmentsList.get(f);
            String[] segUrls = fileUrlsList.get(f);

            // 不同源文件切换处添加 DISCONTINUITY 标记
            if (!firstFile && hasSegments) {
                m3u8.append("#EXT-X-DISCONTINUITY\n");
            }
            firstFile = false;

            for (int i = 0; i < segDurations.size(); i++) {
                double duration = segDurations.get(i)[0];
                m3u8.append(String.format("#EXTINF:%.3f,\n", duration));
                m3u8.append(segUrls[i]).append("\n");
                hasSegments = true;
            }
        }

        double coveredDuration = calculateCoveredDuration(videos, startTime, endTime);
        double totalDuration = endTime - startTime;

        String status;
        if (totalFiles == 0 || readyFiles == 0) {
            status = "preparing";
        } else if (!allReady) {
            status = "partial";
        } else {
            status = "ready";
        }

        PlaybackStatusVo statusVo = new PlaybackStatusVo(
                buildM3u8Url(deviceUid, startTime, endTime),
                status, totalFiles, readyFiles, coveredDuration, totalDuration);

        if (!hasSegments) {
            return new PlaybackM3u8ResultVo(null, statusVo);
        }

        // 所有文件就绪时才添加 ENDLIST，否则 hls.js 会按直播流逻辑轮询获取新分片
        if (allReady) {
            m3u8.append("#EXT-X-ENDLIST\n");
        }
        return new PlaybackM3u8ResultVo(m3u8.toString(), statusVo);
    }

    @Override
    public PlaybackStatusVo getPlaybackStatus(String deviceUid, Long startTime, Long endTime) {
        PlaybackM3u8ResultVo result = getPlaybackM3u8WithStatus(deviceUid, startTime, endTime);
        return result.status();
    }

    /**
     * 下发单个文件的 uploadFile RPC
     */
    private void triggerSingleUpload(String deviceUid, CameraVideo video) {
        Device device = deviceService.getDeviceOrThrow(deviceUid);
        if (!Integer.valueOf(1).equals(device.getStatus())) {
            log.warn("回放触发上传跳过，设备不在线：deviceUid={}", deviceUid);
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("fileId", video.getId());
        params.put("fileType", "video");
        params.put("filePath", video.getFilePath());
        thingsBoardDeviceClient.rpcOneWay(device.getTbDeviceId(),
                RpcBody.of(DeviceRpcMethod.UPLOAD_FILE.getMethod(), params));
        cameraVideoService.updateStatus(video.getId(), FileStatus.PREPARING);
        log.info("回放触发文件上传：deviceUid={}, fileId={}, name={}", deviceUid, video.getId(), video.getName());
    }

    /**
     * 计算从开头连续就绪文件的覆盖时长（遇到第一个未就绪即停止）
     */
    private double calculateCoveredDuration(List<CameraVideo> videos, long rangeStart, long rangeEnd) {
        double covered = 0;
        for (CameraVideo video : videos) {
            if (video.getStartTime() == null || video.getDuration() == null) {
                continue;
            }
            // 只计算连续就绪的文件
            int hlsStatus = video.getHlsStatus() != null ? video.getHlsStatus() : HlsStatus.PENDING;
            if (hlsStatus != HlsStatus.READY) {
                break;
            }
            long fStart = video.getStartTime();
            long fEnd = fStart + video.getDuration();
            long overlapStart = Math.max(fStart, rangeStart);
            long overlapEnd = Math.min(fEnd, rangeEnd);
            if (overlapEnd > overlapStart) {
                covered += (overlapEnd - overlapStart);
            }
        }
        return covered;
    }

    private String buildM3u8Url(String deviceUid, long startTime, long endTime) {
        return "/api/admin/media/hls/" + deviceUid + "/" + startTime + "-" + endTime + "/m3u8";
    }

    private Path resolveCacheDir(String deviceUid, Long fileStartTime, Long fileEndTime) {
        return Paths.get(mediaFileProperties.getBasePath(), DIR_HLS, deviceUid,
                fileStartTime + "-" + fileEndTime);
    }

    @Override
    public List<DailyRecordVo> listDailyRecords(String deviceUid, String date) {
        // 解析日期，计算当天的起止时间戳
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        long dayStart = localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long dayEnd = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        List<CameraVideo> videos = cameraVideoService.findByTimeRange(deviceUid, dayStart, dayEnd);
        List<DailyRecordVo> result = new ArrayList<>();
        for (CameraVideo video : videos) {
            // 只返回 HLS 就绪的文件
            if (video.getStartTime() == null || video.getDuration() == null
                    || video.getStatus() == null || video.getStatus() != FileStatus.PREPARE_SUCCESS) {
                continue;
            }
            int hlsStatus = video.getHlsStatus() != null ? video.getHlsStatus() : HlsStatus.PENDING;
            if (hlsStatus != HlsStatus.READY) {
                continue;
            }
            result.add(DailyRecordVo.from(video));
        }
        return result;
    }
}
