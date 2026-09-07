package com.neurocast.job.task;

import com.neurocast.framework.config.properties.MediaFileProperties;
import com.neurocast.media.domain.vo.CameraImageVo;
import com.neurocast.media.domain.vo.CameraVideoVo;
import com.neurocast.media.service.CameraImageService;
import com.neurocast.media.service.CameraVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 过期媒体清理任务：按保留天数删除过期的图片/录像记录与磁盘文件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaClearTask {

    private static final String DIR_IMAGES = "images";
    private static final String DIR_VIDEOS = "videos";
    private static final String DIR_HLS = "hls";

    /**
     * 文件名日期正则：yyyyMMdd_HHmmss（可选前缀）
     */
    private static final Pattern FILENAME_DATE_PATTERN = Pattern.compile("(\\d{8})_(\\d{2})\\d{4}\\.");

    private final CameraImageService cameraImageService;
    private final CameraVideoService cameraVideoService;
    private final MediaFileProperties mediaFileProperties;

    /**
     * 每天 00:10 清理一次
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void clearData() {
        log.info("开始清理过期媒体文件");
        clearImages();
        clearVideos();
    }

    private void clearImages() {
        long toTime = epochSecondNow() - mediaFileProperties.getImageRetentionDays() * 86400L;
        List<CameraImageVo> list = cameraImageService.findListByEndTime(toTime);
        for (CameraImageVo vo : list) {
            deleteFileQuietly(resolveFilePath(DIR_IMAGES, vo.getDeviceUid(), vo.getName()));
        }
        cameraImageService.clearData(toTime);
        log.info("过期图片清理完成：{} 条", list.size());
    }

    private void clearVideos() {
        long toTime = epochSecondNow() - mediaFileProperties.getVideoRetentionDays() * 86400L;
        List<CameraVideoVo> list = cameraVideoService.findListByEndTime(toTime);
        for (CameraVideoVo vo : list) {
            deleteFileQuietly(resolveFilePath(DIR_VIDEOS, vo.getDeviceUid(), vo.getName()));
            deleteHlsCache(vo);
        }
        cameraVideoService.clearData(toTime);
        log.info("过期录像清理完成：{} 条", list.size());
    }

    /**
     * 删除录像对应的 HLS 缓存目录
     */
    private void deleteHlsCache(CameraVideoVo vo) {
        if (vo.getStartTime() == null || vo.getDuration() == null) {
            return;
        }
        long endTime = vo.getStartTime() + vo.getDuration();
        Path hlsDir = Paths.get(mediaFileProperties.getBasePath(), DIR_HLS,
                vo.getDeviceUid(), vo.getStartTime() + "-" + endTime);
        if (Files.exists(hlsDir)) {
            deleteDirectoryQuietly(hlsDir);
        }
    }

    private void deleteDirectoryQuietly(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            log.warn("删除 HLS 缓存文件失败：{}", path, e);
                        }
                    });
        } catch (Exception e) {
            log.warn("遍历 HLS 缓存目录失败：{}", dir, e);
        }
    }

    private long epochSecondNow() {
        return System.currentTimeMillis() / 1000;
    }

    private void deleteFileQuietly(Path filepath) {
        try {
            Files.deleteIfExists(filepath);
        } catch (Exception e) {
            log.error("删除过期文件失败：{}", filepath, e);
        }
    }

    /**
     * 解析文件路径，优先新目录结构（带日期子目录），回退旧结构
     */
    private Path resolveFilePath(String fileType, String deviceUid, String filename) {
        String dateSubDir = parseDateSubDir(filename);
        if (dateSubDir != null) {
            Path newPath = Paths.get(mediaFileProperties.getBasePath(), fileType, deviceUid, dateSubDir, filename).normalize();
            if (Files.exists(newPath)) {
                return newPath;
            }
        }
        return Paths.get(mediaFileProperties.getBasePath(), fileType, deviceUid, filename).normalize();
    }

    private String parseDateSubDir(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        Matcher matcher = FILENAME_DATE_PATTERN.matcher(filename);
        if (matcher.find()) {
            return matcher.group(1) + "/" + matcher.group(2);
        }
        return null;
    }
}
