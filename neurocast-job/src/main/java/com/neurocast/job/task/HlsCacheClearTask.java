package com.neurocast.job.task;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neurocast.framework.config.properties.HlsProperties;
import com.neurocast.framework.config.properties.MediaFileProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HLS 缓存过期清理任务。
 * 扫描 {basePath}/hls/ 目录，删除超过保留时长的缓存目录。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HlsCacheClearTask {

    private static final String DIR_HLS = "hls";

    private final MediaFileProperties mediaFileProperties;
    private final HlsProperties hlsProperties;

    /**
     * 每小时清理一次过期 HLS 缓存
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void clearHlsCache() {
        Path hlsRoot = Paths.get(mediaFileProperties.getBasePath(), DIR_HLS);
        if (!Files.exists(hlsRoot) || !Files.isDirectory(hlsRoot)) {
            return;
        }

        long retentionMillis = TimeUnit.HOURS.toMillis(hlsProperties.getCacheRetentionHours());
        long now = System.currentTimeMillis();
        int deletedCount = 0;

        try (DirectoryStream<Path> deviceDirs = Files.newDirectoryStream(hlsRoot)) {
            for (Path deviceDir : deviceDirs) {
                if (!Files.isDirectory(deviceDir)) {
                    continue;
                }
                try (DirectoryStream<Path> cacheDirs = Files.newDirectoryStream(deviceDir)) {
                    for (Path cacheDir : cacheDirs) {
                        if (!Files.isDirectory(cacheDir)) {
                            continue;
                        }
                        long lastModified = Files.getLastModifiedTime(cacheDir).toMillis();
                        if (now - lastModified > retentionMillis) {
                            deleteDirectory(cacheDir);
                            deletedCount++;
                        }
                    }
                }
                // 清理空的设备目录
                try (DirectoryStream<Path> check = Files.newDirectoryStream(deviceDir)) {
                    if (!check.iterator().hasNext()) {
                        Files.deleteIfExists(deviceDir);
                    }
                }
            }
        } catch (IOException e) {
            log.error("HLS 缓存清理异常：{}", hlsRoot, e);
            return;
        }

        if (deletedCount > 0) {
            log.info("HLS 缓存清理完成：删除 {} 个过期目录", deletedCount);
        }
    }

    private void deleteDirectory(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("删除 HLS 缓存文件失败：{}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("遍历 HLS 缓存目录失败：{}", dir, e);
        }
    }
}
