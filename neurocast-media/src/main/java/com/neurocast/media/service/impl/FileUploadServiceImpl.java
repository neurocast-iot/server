package com.neurocast.media.service.impl;

import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.RedisKeys;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.utils.IdUtils;
import com.neurocast.framework.config.properties.MediaFileProperties;
import com.neurocast.media.domain.dto.FileMergeDto;
import com.neurocast.media.domain.dto.FileUploadInitDto;
import com.neurocast.media.domain.vo.FileUploadInitVo;
import com.neurocast.media.domain.vo.FileUploadVo;
import com.neurocast.media.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

/**
 * 文件上传服务实现：分片上传（断点续传）、秒传、批量上传
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private static final String CHUNK_DIR_NAME = ".chunks";

    /**
     * 秒传哈希记录保留天数
     */
    private static final long FILE_HASH_RETENTION_DAYS = 30;

    /**
     * 文件名日期正则：yyyyMMdd_HHmmss（可选前缀）
     */
    private static final Pattern FILENAME_DATE_PATTERN = Pattern.compile("(\\d{8})_(\\d{2})\\d{4}\\.");

    private final MediaFileProperties mediaFileProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public FileUploadInitVo initUpload(FileUploadInitDto initDto) {
        FileUploadInitVo initVo = new FileUploadInitVo();

        // 秒传检测
        if (StringUtils.hasText(initDto.getFileHash())) {
            Object existPath = redisTemplate.opsForValue().get(RedisKeys.File.FILE_HASH + initDto.getFileHash());
            if (existPath != null) {
                log.info("文件已存在，秒传完成：fileHash={}", initDto.getFileHash());
                initVo.setInstantComplete(true);
                initVo.setFilePath(existPath.toString());
                return initVo;
            }
        }

        String uploadId = IdUtils.simpleUuid();
        initVo.setUploadId(uploadId);
        initVo.setInstantComplete(false);
        initVo.setUploadedChunks(getUploadedChunks(uploadId));

        // 保存上传任务信息
        String taskKey = RedisKeys.File.UPLOAD_TASK + uploadId;
        String chunkKey = RedisKeys.File.UPLOAD_CHUNK + uploadId;
        Map<String, String> taskInfo = new HashMap<>();
        taskInfo.put("filename", initDto.getFilename());
        taskInfo.put("totalSize", String.valueOf(initDto.getTotalSize()));
        taskInfo.put("fileHash", initDto.getFileHash() == null ? "" : initDto.getFileHash());
        taskInfo.put("totalChunks", String.valueOf(initDto.getTotalChunks()));
        taskInfo.put("subDir", initDto.getSubDir() == null ? "" : initDto.getSubDir());
        taskInfo.put("createTime", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(taskKey, taskInfo);
        redisTemplate.expire(taskKey, mediaFileProperties.getChunkExpireHours(), TimeUnit.HOURS);
        redisTemplate.expire(chunkKey, mediaFileProperties.getChunkExpireHours(), TimeUnit.HOURS);

        // 创建分片临时目录
        try {
            Files.createDirectories(chunkDir(uploadId));
        } catch (IOException e) {
            log.error("创建分片目录失败：uploadId={}", uploadId, e);
            throw new ServiceException(ApiStatus.ERROR, "创建上传任务失败");
        }
        log.info("分片上传初始化完成：uploadId={}, filename={}", uploadId, initDto.getFilename());
        return initVo;
    }

    @Override
    public boolean uploadChunk(String uploadId, Integer chunkIndex, MultipartFile chunkFile) throws IOException {
        Map<Object, Object> taskInfo = redisTemplate.opsForHash().entries(RedisKeys.File.UPLOAD_TASK + uploadId);
        if (taskInfo.isEmpty()) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_UPLOAD_TASK_EXPIRED);
        }

        Path chunkDir = chunkDir(uploadId);
        Files.createDirectories(chunkDir);
        Files.copy(chunkFile.getInputStream(), chunkDir.resolve("chunk_" + chunkIndex),
                StandardCopyOption.REPLACE_EXISTING);
        redisTemplate.opsForSet().add(RedisKeys.File.UPLOAD_CHUNK + uploadId, String.valueOf(chunkIndex));
        return true;
    }

    @Override
    public FileUploadVo mergeChunks(FileMergeDto mergeDto) throws IOException {
        String taskKey = RedisKeys.File.UPLOAD_TASK + mergeDto.getUploadId();
        Map<Object, Object> taskInfo = redisTemplate.opsForHash().entries(taskKey);
        if (taskInfo.isEmpty()) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_UPLOAD_TASK_EXPIRED);
        }

        String filename = (String) taskInfo.get("filename");
        String fileHash = (String) taskInfo.get("fileHash");
        int totalChunks = Integer.parseInt((String) taskInfo.get("totalChunks"));
        String subDir = (String) taskInfo.get("subDir");

        // 校验分片完整性
        String chunkKey = RedisKeys.File.UPLOAD_CHUNK + mergeDto.getUploadId();
        Long uploadedCount = redisTemplate.opsForSet().size(chunkKey);
        if (uploadedCount == null || uploadedCount < totalChunks) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_CHUNK_INCOMPLETE,
                    "分片未上传完整，已上传：" + uploadedCount + "，总数：" + totalChunks);
        }

        // 合并到目标目录（按文件名日期建子目录）
        Path targetDir = StringUtils.hasText(subDir)
                ? Paths.get(mediaFileProperties.getBasePath(), subDir)
                : Paths.get(mediaFileProperties.getBasePath());
        String dateSubDir = parseDateSubDir(filename);
        Path finalDir = StringUtils.hasText(dateSubDir) ? targetDir.resolve(dateSubDir) : targetDir;
        Files.createDirectories(finalDir);
        Path targetPath = finalDir.resolve(filename);

        Path chunkDir = chunkDir(mergeDto.getUploadId());
        try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
            for (int i = 0; i < totalChunks; i++) {
                Path chunkPath = chunkDir.resolve("chunk_" + i);
                if (!Files.exists(chunkPath)) {
                    throw new ServiceException(ApiStatus.BUSINESS_FILE_CHUNK_INCOMPLETE, "分片缺失：chunk_" + i);
                }
                Files.copy(chunkPath, outputStream);
            }
        }

        // MD5 校验
        if (StringUtils.hasText(fileHash)) {
            String actualHash;
            try (InputStream is = Files.newInputStream(targetPath)) {
                actualHash = DigestUtils.md5DigestAsHex(is);
            }
            if (!fileHash.equalsIgnoreCase(actualHash)) {
                Files.deleteIfExists(targetPath);
                throw new ServiceException(ApiStatus.BUSINESS_FILE_HASH_MISMATCH);
            }
        }

        // 清理临时分片与任务
        deleteChunkDir(chunkDir);
        redisTemplate.delete(taskKey);
        redisTemplate.delete(chunkKey);

        // 记录文件哈希（秒传）
        String relativePath = buildRelativePath(subDir, dateSubDir, filename);
        if (StringUtils.hasText(fileHash)) {
            redisTemplate.opsForValue().set(RedisKeys.File.FILE_HASH + fileHash, relativePath,
                    FILE_HASH_RETENTION_DAYS, TimeUnit.DAYS);
        }

        FileUploadVo vo = new FileUploadVo();
        vo.setFilename(filename);
        vo.setFileSize(Files.size(targetPath));
        vo.setFilePath(relativePath);
        vo.setFileHash(fileHash);
        log.info("分片合并完成：uploadId={}, filename={}, size={}", mergeDto.getUploadId(), filename, vo.getFileSize());
        return vo;
    }

    @Override
    public List<FileUploadVo> batchUpload(MultipartFile[] files, String subDir) throws IOException {
        if (files == null || files.length == 0) {
            throw new ServiceException(ApiStatus.BUSINESS_FILE_UPLOAD_NOT_EMPTY);
        }

        Path targetDir = StringUtils.hasText(subDir)
                ? Paths.get(mediaFileProperties.getBasePath(), subDir)
                : Paths.get(mediaFileProperties.getBasePath());
        Files.createDirectories(targetDir);

        List<FileUploadVo> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.warn("跳过空文件：{}", file.getOriginalFilename());
                continue;
            }
            if (file.getSize() > mediaFileProperties.getUploadMaxSize()) {
                throw new ServiceException(ApiStatus.BUSINESS_FILE_UPLOAD_TOO_LARGE,
                        "文件过大：" + file.getOriginalFilename());
            }

            String originalFilename = file.getOriginalFilename();
            String dateSubDir = parseDateSubDir(originalFilename);
            Path finalDir = StringUtils.hasText(dateSubDir) ? targetDir.resolve(dateSubDir) : targetDir;
            Files.createDirectories(finalDir);
            Path targetPath = finalDir.resolve(originalFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String fileHash;
            try (InputStream is = file.getInputStream()) {
                fileHash = DigestUtils.md5DigestAsHex(is);
            }
            redisTemplate.opsForValue().set(RedisKeys.File.FILE_HASH + fileHash,
                    buildRelativePath(subDir, dateSubDir, originalFilename),
                    FILE_HASH_RETENTION_DAYS, TimeUnit.DAYS);

            FileUploadVo vo = new FileUploadVo();
            vo.setFilename(originalFilename);
            vo.setFileSize(file.getSize());
            vo.setFilePath(buildRelativePath(subDir, dateSubDir, originalFilename));
            vo.setFileHash(fileHash);
            results.add(vo);
            log.info("文件上传完成：filename={}, size={}", originalFilename, file.getSize());
        }
        return results;
    }

    @Override
    public List<Integer> getUploadedChunks(String uploadId) {
        Set<Object> uploadedChunks = redisTemplate.opsForSet().members(RedisKeys.File.UPLOAD_CHUNK + uploadId);
        if (uploadedChunks == null || uploadedChunks.isEmpty()) {
            return Collections.emptyList();
        }
        return uploadedChunks.stream()
                .map(Object::toString)
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
    }

    private Path chunkDir(String uploadId) {
        return Paths.get(mediaFileProperties.getBasePath(), CHUNK_DIR_NAME, uploadId);
    }

    private String buildRelativePath(String subDir, String dateSubDir, String filename) {
        String filePath = StringUtils.hasText(dateSubDir) ? dateSubDir + "/" + filename : filename;
        return StringUtils.hasText(subDir) ? subDir + "/" + filePath : filePath;
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
}
