package com.neurocast.media.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.neurocast.common.core.domain.Result;
import com.neurocast.media.domain.dto.DeviceUploadCompleteDto;
import com.neurocast.media.domain.dto.DeviceUploadInitDto;
import com.neurocast.media.domain.vo.DeviceUploadInitVo;
import com.neurocast.media.domain.vo.DeviceUploadResultVo;
import com.neurocast.media.service.DeviceFileUploadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 设备文件上传接口（S3/OSS 风格）：支持简单直传与分片上传两种模式
 * <p>
 * 认证方式：X-API-KEY 请求头
 */
@RestController
@RequestMapping("/api/device/file/upload")
@RequiredArgsConstructor
public class DeviceFileUploadController {

    private final DeviceFileUploadService deviceFileUploadService;

    /**
     * 简单上传（小文件直传，单次请求完成）
     */
    @PostMapping("/simple")
    public Result<DeviceUploadResultVo> simpleUpload(
            @RequestParam String deviceUid,
            @RequestParam String filename,
            @RequestParam(required = false) String fileHash,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        return Result.ok(deviceFileUploadService.simpleUpload(deviceUid, filename, fileHash, file));
    }

    /**
     * 初始化分片上传（对应 S3 CreateMultipartUpload / OSS InitiateMultipartUpload）
     */
    @PostMapping("/uploads")
    public Result<DeviceUploadInitVo> initMultipart(@Valid @RequestBody DeviceUploadInitDto initDto) {
        return Result.ok(deviceFileUploadService.initMultipart(initDto));
    }

    /**
     * 上传单个分片（对应 S3 UploadPart / OSS UploadPart）
     */
    @PutMapping("/uploads/{uploadId}/parts")
    public Result<Void> uploadPart(@PathVariable String uploadId,
                                   @RequestParam int partNumber,
                                   @RequestBody byte[] partData) throws IOException {
        deviceFileUploadService.uploadPart(uploadId, partNumber,
                new java.io.ByteArrayInputStream(partData));
        return Result.ok(null);
    }

    /**
     * 完成分片上传（对应 S3 CompleteMultipartUpload / OSS CompleteMultipartUpload）
     */
    @PostMapping("/uploads/{uploadId}/complete")
    public Result<DeviceUploadResultVo> completeMultipart(
            @PathVariable String uploadId,
            @Valid @RequestBody DeviceUploadCompleteDto completeDto) throws IOException {
        completeDto.setUploadId(uploadId);
        return Result.ok(deviceFileUploadService.completeMultipart(completeDto));
    }

    /**
     * 中止分片上传（对应 S3 AbortMultipartUpload / OSS AbortMultipartUpload）
     */
    @DeleteMapping("/uploads/{uploadId}")
    public Result<Void> abortMultipart(@PathVariable String uploadId) {
        deviceFileUploadService.abortMultipart(uploadId);
        return Result.ok(null);
    }
}
