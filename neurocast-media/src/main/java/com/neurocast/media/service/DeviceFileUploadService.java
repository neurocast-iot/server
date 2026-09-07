package com.neurocast.media.service;

import com.neurocast.media.domain.dto.DeviceUploadCompleteDto;
import com.neurocast.media.domain.dto.DeviceUploadInitDto;
import com.neurocast.media.domain.vo.DeviceUploadInitVo;
import com.neurocast.media.domain.vo.DeviceUploadResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 设备文件上传服务：支持简单直传与 S3/OSS 风格分片上传
 */
public interface DeviceFileUploadService {

    /**
     * 简单上传（小文件直传，单次请求完成）
     */
    DeviceUploadResultVo simpleUpload(String deviceUid, String filename,
                                      String fileHash,
                                      MultipartFile file) throws IOException;

    /**
     * 初始化分片上传（秒传检测 + 断点续传进度恢复）
     */
    DeviceUploadInitVo initMultipart(DeviceUploadInitDto initDto);

    /**
     * 上传单个分片
     */
    void uploadPart(String uploadId, int partNumber, InputStream partStream) throws IOException;

    /**
     * 完成分片上传（合并所有分片并校验）
     */
    DeviceUploadResultVo completeMultipart(DeviceUploadCompleteDto completeDto) throws IOException;

    /**
     * 中止分片上传（清理临时文件）
     */
    void abortMultipart(String uploadId);
}
