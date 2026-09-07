package com.neurocast.media.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.RedisKeys;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.utils.IdUtils;
import com.neurocast.framework.config.properties.MediaFileProperties;
import com.neurocast.media.domain.dto.DeviceUploadCompleteDto;
import com.neurocast.media.domain.dto.DeviceUploadInitDto;
import com.neurocast.media.domain.vo.DeviceUploadInitVo;
import com.neurocast.media.domain.vo.DeviceUploadResultVo;
import com.neurocast.media.service.DeviceFileUploadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备文件上传服务实现：简单直传 + S3/OSS 风格分片上传
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceFileUploadServiceImpl implements DeviceFileUploadService {

    private static final String DIR_IMAGES = "images";
    private static final String DIR_VIDEOS = "videos";
    private static final String CHUNK_DIR_NAME = ".chunks";
    private static final long FILE_HASH_RETENTION_HOURS = 4;
    private static final Pattern FILENAME_DATE_PATTERN = Pattern.compile("(\\d{8})_(\\d{2})\\d{4,7}(?:\\.|_)");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".bmp", ".gif");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(".mp4", ".avi", ".mov", ".mkv", ".flv", ".ts");

    private final MediaFileProperties mediaFileProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public DeviceUploadResultVo simpleUpload(String deviceUid, String filename,
                                             String fileHash,
                                             MultipartFile file) throws IOException {
        // 秒传检测（优先于文件校验，设备可能只传 hash 试探）
        if (StringUtils.hasText(fileHash)) {
            Object existPath = redisTemplate.opsForValue().get(RedisKeys.File.FILE_HASH + fileHash);
            if (existPath != null) {
                log.info("设备文件已存在，秒传完成：fileHash={}", fileHash);
                DeviceUploadResultVo vo = new DeviceUploadResultVo();
                vo.setFilename(filename);
                vo.setFilePath(existPath.toString());
                vo.setFileSize(file != null ? file.getSize() : 0L);
                vo.setFileHash(fileHash);
                return vo;
            }
        }

        // 未命中秒传，文件必须存在
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_UPLOAD_NOT_EMPTY);
        }
        if (file.getSize() > mediaFileProperties.getUploadMaxSize()) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_UPLOAD_TOO_LARGE);
        }

        // 存储到 images|videos/{deviceUid}/{dateSubDir}/{filename}
        String fileType = resolveFileType(filename);
        Path targetDir = resolveDeviceDir(fileType, deviceUid, filename);
        Files.createDirectories(targetDir);
        Path targetPath = targetDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 计算 MD5
        String actualHash;
        try (InputStream is = Files.newInputStream(targetPath)) {
            actualHash = DigestUtils.md5DigestAsHex(is);
        }

        // 记录秒传哈希
        String relativePath = buildRelativePath(fileType, deviceUid, filename);
        redisTemplate.opsForValue().set(RedisKeys.File.FILE_HASH + actualHash, relativePath,
                FILE_HASH_RETENTION_HOURS, TimeUnit.HOURS);

        DeviceUploadResultVo vo = new DeviceUploadResultVo();
        vo.setFilename(filename);
        vo.setFileSize(Files.size(targetPath));
        vo.setFilePath(relativePath);
        vo.setFileHash(actualHash);
        log.info("设备文件简单上传完成：deviceUid={}, filename={}, path={}, size={}", deviceUid, filename, targetPath, vo.getFileSize());
        return vo;
    }

    @Override
    public DeviceUploadInitVo initMultipart(DeviceUploadInitDto initDto) {
        DeviceUploadInitVo initVo = new DeviceUploadInitVo();

        // 秒传检测
        if (StringUtils.hasText(initDto.getFileHash())) {
            Object existPath = redisTemplate.opsForValue().get(RedisKeys.File.FILE_HASH + initDto.getFileHash());
            if (existPath != null) {
                log.info("设备文件已存在，秒传完成：fileHash={}", initDto.getFileHash());
                initVo.setInstantComplete(true);
                initVo.setFilePath(existPath.toString());
                return initVo;
            }
        }

        String uploadId = IdUtils.simpleUuid();
        initVo.setUploadId(uploadId);
        initVo.setInstantComplete(false);
        initVo.setUploadedParts(getUploadedParts(uploadId));

        // 保存上传任务信息到 Redis
        String taskKey = RedisKeys.File.UPLOAD_TASK + uploadId;
        String partKey = RedisKeys.File.UPLOAD_CHUNK + uploadId;
        Map<String, String> taskInfo = new HashMap<>();
        taskInfo.put("deviceUid", initDto.getDeviceUid());
        taskInfo.put("filename", initDto.getFilename());
        taskInfo.put("fileSize", String.valueOf(initDto.getFileSize()));
        taskInfo.put("fileHash", initDto.getFileHash() == null ? "" : initDto.getFileHash());
        taskInfo.put("totalParts", String.valueOf(initDto.getTotalParts()));
        taskInfo.put("createTime", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(taskKey, taskInfo);
        redisTemplate.expire(taskKey, mediaFileProperties.getChunkExpireHours(), TimeUnit.HOURS);
        redisTemplate.expire(partKey, mediaFileProperties.getChunkExpireHours(), TimeUnit.HOURS);

        // 创建分片临时目录
        try {
            Files.createDirectories(chunkDir(uploadId));
        } catch (IOException e) {
            log.error("创建分片目录失败：uploadId={}", uploadId, e);
            throw new ServiceException(ApiStatus.ERROR, "创建上传任务失败");
        }
        log.info("设备分片上传初始化：uploadId={}, deviceUid={}, filename={}",
                uploadId, initDto.getDeviceUid(), initDto.getFilename());
        return initVo;
    }

    @Override
    public void uploadPart(String uploadId, int partNumber, InputStream partStream) throws IOException {
        Map<Object, Object> taskInfo = redisTemplate.opsForHash().entries(RedisKeys.File.UPLOAD_TASK + uploadId);
        if (taskInfo.isEmpty()) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_UPLOAD_TASK_EXPIRED);
        }

        Path chunkDir = chunkDir(uploadId);
        Files.createDirectories(chunkDir);
        Files.copy(partStream, chunkDir.resolve("part_" + partNumber), StandardCopyOption.REPLACE_EXISTING);
        redisTemplate.opsForSet().add(RedisKeys.File.UPLOAD_CHUNK + uploadId, String.valueOf(partNumber));
    }

    @Override
    public DeviceUploadResultVo completeMultipart(DeviceUploadCompleteDto completeDto) throws IOException {
        String taskKey = RedisKeys.File.UPLOAD_TASK + completeDto.getUploadId();
        Map<Object, Object> taskInfo = redisTemplate.opsForHash().entries(taskKey);
        if (taskInfo.isEmpty()) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_UPLOAD_TASK_EXPIRED);
        }

        String deviceUid = (String) taskInfo.get("deviceUid");
        String filename = (String) taskInfo.get("filename");
        String fileHash = (String) taskInfo.get("fileHash");
        int totalParts = Integer.parseInt((String) taskInfo.get("totalParts"));

        // 校验分片完整性
        String partKey = RedisKeys.File.UPLOAD_CHUNK + completeDto.getUploadId();
        Long uploadedCount = redisTemplate.opsForSet().size(partKey);
        if (uploadedCount == null || uploadedCount < totalParts) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_CHUNK_INCOMPLETE,
                    "分片未上传完整，已上传：" + uploadedCount + "，总数：" + totalParts);
        }

        // 合并到目标目录
        String fileType = resolveFileType(filename);
        Path targetDir = resolveDeviceDir(fileType, deviceUid, filename);
        Files.createDirectories(targetDir);
        Path targetPath = targetDir.resolve(filename);

        Path chunkDir = chunkDir(completeDto.getUploadId());
        try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
            for (int i = 0; i < totalParts; i++) {
                Path partPath = chunkDir.resolve("part_" + i);
                if (!Files.exists(partPath)) {
                    throw new ServiceException(ApiStatus.BUSINESS_FILE_CHUNK_INCOMPLETE, "分片缺失：part_" + i);
                }
                Files.copy(partPath, outputStream);
            }
        }

        // MD5 校验
        String actualHash = null;
        if (StringUtils.hasText(fileHash)) {
            try (InputStream is = Files.newInputStream(targetPath)) {
                actualHash = DigestUtils.md5DigestAsHex(is);
            }
            if (!fileHash.equalsIgnoreCase(actualHash)) {
                Files.deleteIfExists(targetPath);
                throw new ServiceException(ApiStatus.BUSINESS_FILE_HASH_MISMATCH);
            }
        } else {
            // 未提供预期哈希，计算实际哈希
            try (InputStream is = Files.newInputStream(targetPath)) {
                actualHash = DigestUtils.md5DigestAsHex(is);
            }
        }

        // 清理临时分片与任务
        deleteChunkDir(chunkDir);
        redisTemplate.delete(taskKey);
        redisTemplate.delete(partKey);

        // 记录秒传哈希
        String relativePath = buildRelativePath(fileType, deviceUid, filename);
        redisTemplate.opsForValue().set(RedisKeys.File.FILE_HASH + actualHash, relativePath,
                FILE_HASH_RETENTION_HOURS, TimeUnit.HOURS);

        DeviceUploadResultVo vo = new DeviceUploadResultVo();
        vo.setFilename(filename);
        vo.setFileSize(Files.size(targetPath));
        vo.setFilePath(relativePath);
        vo.setFileHash(actualHash);
        log.info("设备分片上传完成：uploadId={}, deviceUid={}, filename={}, path={}, size={}",
                completeDto.getUploadId(), deviceUid, filename, targetPath, vo.getFileSize());
        return vo;
    }

    @Override
    public void abortMultipart(String uploadId) {
        Path chunkDir = chunkDir(uploadId);
        deleteChunkDir(chunkDir);
        redisTemplate.delete(RedisKeys.File.UPLOAD_TASK + uploadId);
        redisTemplate.delete(RedisKeys.File.UPLOAD_CHUNK + uploadId);
        log.info("设备分片上传已中止并清理：uploadId={}", uploadId);
    }

    /**
     * 根据文件名后缀推断文件类型目录：images 或 videos
     */
    private String resolveFileType(String filename) {
        String lower = filename.toLowerCase();
        int dot = lower.lastIndexOf('.');
        if (dot >= 0) {
            String ext = lower.substring(dot);
            if (IMAGE_EXTENSIONS.contains(ext)) {
                return DIR_IMAGES;
            }
            if (VIDEO_EXTENSIONS.contains(ext)) {
                return DIR_VIDEOS;
            }
        }
        // 默认归入 images
        return DIR_IMAGES;
    }

    /**
     * 解析文件存储目录：basePath/{images|videos}/{deviceUid}/{dateSubDir}
     */
    private Path resolveDeviceDir(String fileType, String deviceUid, String filename) {
        Path base = Paths.get(mediaFileProperties.getBasePath(), fileType, deviceUid);
        String dateSubDir = parseDateSubDir(filename);
        return StringUtils.hasText(dateSubDir) ? base.resolve(dateSubDir) : base;
    }

    /**
     * 构建相对路径：{images|videos}/{deviceUid}/{dateSubDir}/{filename}
     */
    private String buildRelativePath(String fileType, String deviceUid, String filename) {
        String dateSubDir = parseDateSubDir(filename);
        String filePath = StringUtils.hasText(dateSubDir) ? dateSubDir + "/" + filename : filename;
        return fileType + "/" + deviceUid + "/" + filePath;
    }

    private Path chunkDir(String uploadId) {
        return Paths.get(mediaFileProperties.getBasePath(), CHUNK_DIR_NAME, uploadId);
    }

    private List<Integer> getUploadedParts(String uploadId) {
        Set<Object> parts = redisTemplate.opsForSet().members(RedisKeys.File.UPLOAD_CHUNK + uploadId);
        if (parts == null || parts.isEmpty()) {
            return Collections.emptyList();
        }
        return parts.stream().map(Object::toString).map(Integer::parseInt).sorted().collect(Collectors.toList());
    }

    private String parseDateSubDir(String filename) {
        if (!StringUtils.hasText(filename)) {
            return null;
        }
        Matcher matcher = FILENAME_DATE_PATTERN.matcher(filename);
        if (matcher.find()) {
            return matcher.group(1) + "/" + matcher.group(2);
        }
        return null;
    }

    private void deleteChunkDir(Path chunkDir) {
        try {
            if (Files.exists(chunkDir)) {
                try (Stream<Path> stream = Files.walk(chunkDir)) {
                    stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("删除临时文件失败：{}", path, e);
                        }
                    });
                }
            }
        } catch (IOException e) {
            log.warn("清理分片目录失败：{}", chunkDir, e);
        }
    }
}
