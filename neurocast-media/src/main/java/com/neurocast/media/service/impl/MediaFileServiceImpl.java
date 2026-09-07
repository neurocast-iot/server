package com.neurocast.media.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.FileStatus;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.device.domain.Device;
import com.neurocast.device.protocol.DeviceRpcMethod;
import com.neurocast.device.service.DeviceService;
import com.neurocast.framework.config.properties.MediaFileProperties;
import com.neurocast.framework.integration.thingsboard.RpcBody;
import com.neurocast.framework.integration.thingsboard.ThingsBoardDeviceClient;
import com.neurocast.media.domain.dto.FilePrepareDto;
import com.neurocast.media.domain.vo.CameraImageVo;
import com.neurocast.media.domain.vo.CameraVideoVo;
import com.neurocast.media.domain.vo.FilePrepareResultVo;
import com.neurocast.media.service.CameraImageService;
import com.neurocast.media.service.CameraVideoService;
import com.neurocast.media.service.MediaFileService;
import com.neurocast.media.service.MediaUrlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件服务实现：上传准备（RPC 通知设备上传）+ 服务端文件下载
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    private static final String FILE_TYPE_IMAGE = "image";
    private static final String FILE_TYPE_VIDEO = "video";
    private static final String DIR_IMAGES = "images";
    private static final String DIR_VIDEOS = "videos";

    /**
     * 文件名日期正则：yyyyMMdd_HHmmss（可选前缀）
     */
    private static final Pattern FILENAME_DATE_PATTERN = Pattern.compile("(\\d{8})_(\\d{2})\\d{4,7}(?:\\.|_)");

    private final DeviceService deviceService;
    private final CameraImageService cameraImageService;
    private final CameraVideoService cameraVideoService;
    private final ThingsBoardDeviceClient thingsBoardDeviceClient;
    private final MediaFileProperties mediaFileProperties;
    private final MediaUrlService mediaUrlService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public FilePrepareResultVo prepareFile(FilePrepareDto dto) {
        FilePrepareResultVo resultVo = new FilePrepareResultVo();
        String fileId = null;
        String filePath = null;
        DeviceRpcMethod method = null;

        if (FILE_TYPE_IMAGE.equals(dto.getFileType())) {
            CameraImageVo imageVo = cameraImageService.findByDeviceUidAndName(dto.getDeviceUid(), dto.getFilename());
            if (imageVo == null) {
                throw new ServiceException(ApiStatus.BUSINESS_FILE_NOT_EXISTED);
            }
            if (FileStatus.INIT == imageVo.getStatus() || FileStatus.PREPARE_FAILED == imageVo.getStatus()) {
                fileId = imageVo.getId();
                filePath = imageVo.getFilePath();
                method = DeviceRpcMethod.UPLOAD_FILE;
            }
            resultVo.setId(imageVo.getId());
            resultVo.setFileSize(imageVo.getFileSize());
            resultVo.setName(imageVo.getName());
            resultVo.setEventTime(imageVo.getEventTime());
            resultVo.setStatus(imageVo.getStatus());
            if (FileStatus.PREPARE_SUCCESS == imageVo.getStatus()) {
                resultVo.setFileUrl(mediaUrlService.genDownloadUrl(DIR_IMAGES, imageVo.getDeviceUid(), imageVo.getName()));
            }
        } else if (FILE_TYPE_VIDEO.equals(dto.getFileType())) {
            CameraVideoVo videoVo = cameraVideoService.findByDeviceUidAndName(dto.getDeviceUid(), dto.getFilename());
            if (videoVo == null) {
                throw new ServiceException(ApiStatus.BUSINESS_FILE_NOT_EXISTED);
            }
            if (FileStatus.INIT == videoVo.getStatus() || FileStatus.PREPARE_FAILED == videoVo.getStatus()) {
                fileId = videoVo.getId();
                filePath = videoVo.getFilePath();
                method = DeviceRpcMethod.UPLOAD_FILE;
            }
            resultVo.setId(videoVo.getId());
            resultVo.setFileSize(videoVo.getFileSize());
            resultVo.setName(videoVo.getName());
            resultVo.setEventTime(videoVo.getEventTime());
            resultVo.setStatus(videoVo.getStatus());
            if (FileStatus.PREPARE_SUCCESS == videoVo.getStatus()) {
                resultVo.setFileUrl(mediaUrlService.genDownloadUrl(DIR_VIDEOS, videoVo.getDeviceUid(), videoVo.getName()));
            }
        } else {
            throw new ServiceException(ApiStatus.PARAMETER_FORMAT_ERROR, "不支持的文件类型：" + dto.getFileType());
        }

        // 需要设备上传时：校验设备在线并下发上传 RPC
        if (StringUtils.hasText(fileId)) {
            Device device = deviceService.getDeviceOrThrow(dto.getDeviceUid());
            if (!Integer.valueOf(1).equals(device.getStatus())) {
                throw new ServiceException(ApiStatus.BUSINESS_DEVICE_NOT_ONLINE);
            }
            Map<String, Object> params = new HashMap<>();
            params.put("fileId", fileId);
            params.put("fileType", dto.getFileType());
            params.put("filePath", filePath);
            thingsBoardDeviceClient.rpcOneWay(device.getTbDeviceId(), RpcBody.of(method.getMethod(), params));

            if (FILE_TYPE_IMAGE.equals(dto.getFileType())) {
                cameraImageService.updateStatus(fileId, FileStatus.PREPARING);
            } else {
                cameraVideoService.updateStatus(fileId, FileStatus.PREPARING);
            }
            resultVo.setStatus(FileStatus.PREPARING);
        }
        return resultVo;
    }

    @Override
    public ResponseEntity<StreamingResponseBody> download(String fileType, String deviceUid, String filename,
                                                          String rangeHeader) {
        Path filepath = resolveFilePath(deviceUid, fileType, filename);
        if (!Files.exists(filepath)) {
            log.warn("文件不存在：{}", filepath);
            return ResponseEntity.notFound().build();
        }

        try {
            long fileSize = Files.size(filepath);
            String mimeType = Files.probeContentType(filepath);
            if (mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            // 范围请求（断点续传）
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(filepath, fileSize, mimeType, rangeHeader);
            }

            StreamingResponseBody stream = outputStream -> {
                try (InputStream inputStream = Files.newInputStream(filepath)) {
                    byte[] buffer = new byte[64 * 1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        try {
                            outputStream.write(buffer, 0, bytesRead);
                            outputStream.flush();
                        } catch (IOException e) {
                            log.debug("客户端中断下载：{}", filename);
                            break;
                        }
                    }
                }
            };

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .contentLength(fileSize)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
                    .body(stream);
        } catch (IOException e) {
            log.error("文件下载失败：{}", filepath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 处理 HTTP Range 请求（断点续传）
     */
    private ResponseEntity<StreamingResponseBody> handleRangeRequest(Path filepath, long fileSize,
                                                                     String mimeType, String rangeHeader) {
        String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
        long start = Long.parseLong(ranges[0]);
        long end = (ranges.length > 1 && !ranges[1].isEmpty())
                ? Long.parseLong(ranges[1])
                : fileSize - 1;
        if (end >= fileSize) {
            end = fileSize - 1;
        }
        long contentLength = end - start + 1;

        StreamingResponseBody stream = outputStream -> {
            try (RandomAccessFile raf = new RandomAccessFile(filepath.toFile(), "r")) {
                raf.seek(start);
                byte[] buffer = new byte[64 * 1024];
                long bytesRemaining = contentLength;
                while (bytesRemaining > 0) {
                    int read = raf.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining));
                    if (read == -1) {
                        break;
                    }
                    try {
                        outputStream.write(buffer, 0, read);
                        bytesRemaining -= read;
                    } catch (IOException e) {
                        log.debug("Range 请求中断：{}", filepath.getFileName());
                        break;
                    }
                }
            }
        };

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                .contentLength(contentLength)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filepath.getFileName() + "\"")
                .body(stream);
    }

    /**
     * 解析文件路径，支持新旧两种目录结构：
     * 新路径 {basePath}/{fileType}/{deviceUid}/{yyyyMMdd}/{HH}/{filename}，
     * 旧路径 {basePath}/{fileType}/{deviceUid}/{filename}，优先新路径。
     */
    private Path resolveFilePath(String deviceUid, String fileType, String filename) {
        String dateSubDir = parseDateSubDir(filename);
        if (dateSubDir != null) {
            Path newPath = Paths.get(mediaFileProperties.getBasePath(), fileType, deviceUid, dateSubDir, filename).normalize();
            if (Files.exists(newPath)) {
                return newPath;
            }
        }
        return Paths.get(mediaFileProperties.getBasePath(), fileType, deviceUid, filename).normalize();
    }

    /**
     * 从文件名解析日期子目录，如 interval_20260703_172614.jpg -> 20260703/17
     */
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
