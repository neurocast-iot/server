package com.neurocast.media.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.neurocast.common.core.constant.HlsStatus;
import com.neurocast.framework.config.properties.HlsProperties;
import com.neurocast.framework.config.properties.MediaFileProperties;
import com.neurocast.media.domain.CameraVideo;
import com.neurocast.media.service.CameraVideoService;
import com.neurocast.media.service.HlsSegmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HLS 分片服务实现。
 * 使用 FFmpeg 将上传完成的视频文件分片为 TS，生成 manifest.json 和 m3u8。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HlsSegmentServiceImpl implements HlsSegmentService {

    private static final String DIR_VIDEOS = "videos";
    private static final String DIR_HLS = "hls";
    private static final String MANIFEST_FILE = "manifest.json";
    private static final String M3U8_FILE = "m3u8.m3u8";
    private static final Pattern FILENAME_DATE_PATTERN = Pattern.compile("(\\d{8})_(\\d{2})\\d{4,7}(?:\\.|_)");
    private static final Pattern EXTINF_PATTERN = Pattern.compile("#EXTINF:([\\d.]+)");

    private final MediaFileProperties mediaFileProperties;
    private final HlsProperties hlsProperties;
    private final CameraVideoService cameraVideoService;

    @Async("eventTaskExecutor")
    @Override
    public void processFileAsync(CameraVideo cameraVideo) {
        String deviceUid = cameraVideo.getDeviceUid();
        Long startTime = cameraVideo.getStartTime();
        Integer duration = cameraVideo.getDuration();

        if (startTime == null || duration == null || duration <= 0) {
            log.warn("HLS 分片跳过：缺少 startTime 或 duration，deviceUid={}, name={}", deviceUid, cameraVideo.getName());
            return;
        }

        long fileEndTime = startTime + duration;
        Path cacheDir = resolveCacheDir(deviceUid, startTime, fileEndTime);

        // DB 状态为 READY 则跳过
        Integer currentHlsStatus = cameraVideo.getHlsStatus();
        if (currentHlsStatus != null && currentHlsStatus == HlsStatus.READY) {
            log.info("HLS 分片已就绪，跳过：deviceUid={}, range={}-{}", deviceUid, startTime, fileEndTime);
            return;
        }

        // 定位服务端已上传的视频文件
        Path videoFile = resolveVideoFilePath(deviceUid, cameraVideo.getName());
        if (videoFile == null || !Files.exists(videoFile)) {
            log.warn("HLS 分片跳过：视频文件不存在，deviceUid={}, name={}", deviceUid, cameraVideo.getName());
            cameraVideoService.updateHlsStatus(cameraVideo.getId(), HlsStatus.FAILED);
            return;
        }

        // 更新状态为分片中
        cameraVideoService.updateHlsStatus(cameraVideo.getId(), HlsStatus.PROCESSING);

        try {
            doSegment(videoFile, cacheDir, cameraVideo.getName(), startTime, fileEndTime);
            cameraVideoService.updateHlsStatus(cameraVideo.getId(), HlsStatus.READY);
            log.info("HLS 分片完成：deviceUid={}, range={}-{}, dir={}", deviceUid, startTime, fileEndTime, cacheDir);
        } catch (Exception e) {
            log.error("HLS 分片失败：deviceUid={}, name={}", deviceUid, cameraVideo.getName(), e);
            cameraVideoService.updateHlsStatus(cameraVideo.getId(), HlsStatus.FAILED);
            // 清理可能产生的临时文件
            cleanupDir(cacheDir);
        }
    }

    @Override
    public String getFileM3u8Content(String deviceUid, Long fileStartTime, Long fileEndTime) {
        Path cacheDir = resolveCacheDir(deviceUid, fileStartTime, fileEndTime);
        Path m3u8Path = cacheDir.resolve(M3U8_FILE);
        if (!Files.exists(m3u8Path)) {
            return null;
        }
        try {
            return Files.readString(m3u8Path);
        } catch (IOException e) {
            log.error("读取 m3u8 失败：{}", m3u8Path, e);
            return null;
        }
    }

    @Override
    public Path getSegmentPath(String deviceUid, Long fileStartTime, Long fileEndTime, String segmentName) {
        Path cacheDir = resolveCacheDir(deviceUid, fileStartTime, fileEndTime);
        return cacheDir.resolve(segmentName);
    }

    /**
     * 执行 FFmpeg 分片并生成 manifest + m3u8
     */
    private void doSegment(Path videoFile, Path cacheDir, String sourceFile,
                           long fileStartTime, long fileEndTime) throws IOException, InterruptedException {
        Path tmpDir = cacheDir.resolve("tmp");
        Files.createDirectories(tmpDir);

        // FFmpeg 分片
        String tmpPattern = tmpDir.resolve("tmp_%04d.ts").toString();
        String tmpM3u8 = tmpDir.resolve("temp.m3u8").toString();

        List<String> command = List.of(
                hlsProperties.getFfmpegPath(),
                "-i", videoFile.toString(),
                "-c:v", "copy",
                "-c:a", "copy",
                "-f", "hls",
                "-hls_time", String.valueOf(hlsProperties.getTargetSegmentDuration()),
                "-hls_list_size", "0",
                "-hls_segment_filename", tmpPattern,
                tmpM3u8
        );

        log.debug("FFmpeg 命令：{}", String.join(" ", command));
        int exitCode = executeCommand(command);
        if (exitCode != 0) {
            throw new IOException("FFmpeg 分片失败，退出码：" + exitCode);
        }

        // 读取 FFmpeg 生成的临时 m3u8，解析分片信息
        List<SegmentInfo> segments = parseTempM3u8(tmpDir);
        if (segments.isEmpty()) {
            throw new IOException("FFmpeg 未产生任何分片");
        }

        // 重命名分片为最终编号（seg_0000.ts, seg_0001.ts, ...）
        List<SegmentInfo> finalSegments = new ArrayList<>();
        long segStartTime = fileStartTime;
        for (int i = 0; i < segments.size(); i++) {
            SegmentInfo tmp = segments.get(i);
            String finalName = String.format("seg_%04d.ts", i);
            Path tmpPath = tmpDir.resolve(tmp.filename);
            Path finalPath = cacheDir.resolve(finalName);
            Files.move(tmpPath, finalPath, StandardCopyOption.REPLACE_EXISTING);

            SegmentInfo seg = new SegmentInfo();
            seg.filename = finalName;
            seg.duration = tmp.duration;
            seg.startTime = segStartTime;
            seg.endTime = segStartTime + Math.round(tmp.duration);
            seg.sourceFile = sourceFile;
            finalSegments.add(seg);

            segStartTime = seg.endTime;
        }

        // 生成 manifest.json
        JSONObject manifest = new JSONObject();
        manifest.put("sourceFile", sourceFile);
        manifest.put("fileStartTime", fileStartTime);
        manifest.put("fileEndTime", fileEndTime);
        manifest.put("status", "ready");
        manifest.put("segments", finalSegments);
        manifest.put("targetSegmentDuration", hlsProperties.getTargetSegmentDuration());
        Files.writeString(cacheDir.resolve(MANIFEST_FILE), manifest.toJSONString());

        // 生成 m3u8
        String m3u8Content = buildM3u8(finalSegments);
        Files.writeString(cacheDir.resolve(M3U8_FILE), m3u8Content);

        // 清理临时目录
        cleanupDir(tmpDir);
    }

    /**
     * 解析 FFmpeg 生成的临时 m3u8，提取分片时长和文件名
     */
    private List<SegmentInfo> parseTempM3u8(Path tmpDir) throws IOException {
        List<SegmentInfo> segments = new ArrayList<>();
        Path tempM3u8 = tmpDir.resolve("temp.m3u8");
        if (!Files.exists(tempM3u8)) {
            return segments;
        }

        List<String> lines = Files.readAllLines(tempM3u8);
        for (int i = 0; i < lines.size(); i++) {
            Matcher matcher = EXTINF_PATTERN.matcher(lines.get(i));
            if (matcher.find() && i + 1 < lines.size()) {
                SegmentInfo seg = new SegmentInfo();
                seg.duration = Double.parseDouble(matcher.group(1));
                seg.filename = lines.get(i + 1).trim();
                segments.add(seg);
            }
        }
        return segments;
    }

    /**
     * 根据分片列表生成 m3u8 文本
     */
    private String buildM3u8(List<SegmentInfo> segments) {
        int targetDuration = hlsProperties.getTargetSegmentDuration();
        StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n");
        sb.append("#EXT-X-VERSION:3\n");
        sb.append("#EXT-X-TARGETDURATION:").append(targetDuration).append("\n");
        sb.append("#EXT-X-MEDIA-SEQUENCE:0\n");
        sb.append("#EXT-X-PLAYLIST-TYPE:VOD\n");
        for (SegmentInfo seg : segments) {
            sb.append(String.format("#EXTINF:%.3f,\n", seg.duration));
            sb.append(seg.filename).append("\n");
        }
        sb.append("#EXT-X-ENDLIST\n");
        return sb.toString();
    }

    /**
     * 执行外部命令并等待完成
     */
    private int executeCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 读取输出防止进程阻塞
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("FFmpeg: {}", line);
            }
        }

        return process.waitFor();
    }

    /**
     * 定位服务端已上传的视频文件路径
     */
    private Path resolveVideoFilePath(String deviceUid, String filename) {
        String dateSubDir = parseDateSubDir(filename);
        Path basePath = Paths.get(mediaFileProperties.getBasePath(), DIR_VIDEOS, deviceUid);
        if (dateSubDir != null) {
            Path newPath = basePath.resolve(dateSubDir).resolve(filename);
            if (Files.exists(newPath)) {
                return newPath;
            }
        }
        Path oldPath = basePath.resolve(filename);
        return Files.exists(oldPath) ? oldPath : null;
    }

    /**
     * HLS 缓存目录：{basePath}/hls/{deviceUid}/{fileStartTime}-{fileEndTime}/
     */
    private Path resolveCacheDir(String deviceUid, Long fileStartTime, Long fileEndTime) {
        return Paths.get(mediaFileProperties.getBasePath(), DIR_HLS, deviceUid,
                fileStartTime + "-" + fileEndTime);
    }

    private String parseDateSubDir(String filename) {
        if (filename == null) {
            return null;
        }
        Matcher matcher = FILENAME_DATE_PATTERN.matcher(filename);
        if (matcher.find()) {
            return matcher.group(1) + "/" + matcher.group(2);
        }
        return null;
    }

    private void cleanupDir(Path dir) {
        try {
            if (Files.exists(dir)) {
                try (var stream = Files.walk(dir)) {
                    stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            log.warn("删除临时文件失败：{}", p, e);
                        }
                    });
                }
            }
        } catch (IOException e) {
            log.warn("清理目录失败：{}", dir, e);
        }
    }

    /**
     * TS 分片信息（内部使用）
     */
    private static class SegmentInfo {
        String filename;
        double duration;
        long startTime;
        long endTime;
        String sourceFile;

        // FastJSON 序列化需要
        public String getFilename() { return filename; }
        public double getDuration() { return duration; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public String getSourceFile() { return sourceFile; }
    }
}
