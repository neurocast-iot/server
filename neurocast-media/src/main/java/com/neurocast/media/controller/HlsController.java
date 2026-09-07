package com.neurocast.media.controller;

import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.Result;
import com.neurocast.media.domain.dto.PlaybackPrepareDto;
import com.neurocast.media.domain.vo.DailyRecordVo;
import com.neurocast.media.domain.vo.PlaybackFileVo;
import com.neurocast.media.domain.vo.PlaybackFilesResultVo;
import com.neurocast.media.service.HlsSegmentService;
import com.neurocast.media.service.PlaybackService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HLS 回放控制器。
 * 提供文件列表查询、单文件上传触发、状态查询、m3u8 获取和 TS 分片下载。
 * 前端编排模式：前端控制文件上传顺序，服务端 m3u8 动态累积已就绪分片。
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
public class HlsController {

    private static final MediaType M3U8_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.apple.mpegurl");
    private static final MediaType TS_MEDIA_TYPE = MediaType.parseMediaType("video/mp2t");

    private final PlaybackService playbackService;
    private final HlsSegmentService hlsSegmentService;

    /**
     * 查询时间段内的录像文件列表（含 HLS 就绪状态和 m3u8Url）
     */
    @GetMapping("/playback/files")
    public Result<PlaybackFilesResultVo> listFiles(
            @RequestParam String deviceUid,
            @RequestParam Long startTime,
            @RequestParam Long endTime) {
        return Result.ok(playbackService.listFiles(deviceUid, startTime, endTime));
    }

    /**
     * 查询某天可播放的录像记录列表（仅返回 HLS 就绪的文件）
     */
    @GetMapping("/playback/daily-records")
    public Result<List<DailyRecordVo>> listDailyRecords(
            @RequestParam String deviceUid,
            @RequestParam String date) {
        return Result.ok(playbackService.listDailyRecords(deviceUid, date));
    }

    /**
     * 触发单个文件上传（前端编排入口）
     */
    @PostMapping("/playback/prepare")
    public Result<PlaybackFileVo> prepareFile(@Valid @RequestBody PlaybackPrepareDto dto) {
        return Result.ok(playbackService.prepareFile(dto));
    }

    /**
     * 获取回放虚拟 m3u8（纯 HLS 端点，符合 HLS 协议标准）。
     * 路径格式：/api/admin/media/hls/{deviceUid}/{startTime}-{endTime}/m3u8
     * <p>
     * 返回所有已就绪文件的 TS 分片累积列表；无就绪分片时返回 404。
     * 前端通过 prepare + 轮询状态控制上传节奏，hls.js 定时刷新此 URL 获取新增分片。
     */
    @GetMapping("/hls/{deviceUid}/{timeRange}/m3u8")
    public ResponseEntity<?> getPlaybackM3u8(
            @PathVariable String deviceUid,
            @PathVariable String timeRange) {

        long[] range = parseTimeRange(timeRange);
        if (range == null) {
            return ResponseEntity.badRequest().body(Result.failed("时间范围格式错误：" + timeRange));
        }

        String m3u8Content = playbackService.getPlaybackM3u8(deviceUid, range[0], range[1]);
        if (m3u8Content == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(M3U8_MEDIA_TYPE)
                .body(m3u8Content);
    }

    /**
     * 获取 TS 分片文件（二进制流）。
     * 路径格式：/api/admin/media/hls/{deviceUid}/{startTime}-{endTime}/{segmentName}
     */
    @GetMapping("/hls/{deviceUid}/{timeRange}/{segmentName}")
    public ResponseEntity<Resource> getSegment(
            @PathVariable String deviceUid,
            @PathVariable String timeRange,
            @PathVariable String segmentName) {

        long[] range = parseTimeRange(timeRange);
        if (range == null) {
            return ResponseEntity.notFound().build();
        }

        Path segmentPath = hlsSegmentService.getSegmentPath(deviceUid, range[0], range[1], segmentName);
        if (!segmentPath.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(segmentPath);
        return ResponseEntity.ok()
                .contentType(TS_MEDIA_TYPE)
                .contentLength(segmentPath.toFile().length())
                .body(resource);
    }

    /**
     * 解析时间范围字符串 "{startTime}-{endTime}"
     */
    private long[] parseTimeRange(String timeRange) {
        try {
            String[] parts = timeRange.split("-");
            if (parts.length != 2) {
                return null;
            }
            return new long[]{Long.parseLong(parts[0]), Long.parseLong(parts[1])};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
