package com.neurocast.media.service;

import com.neurocast.media.domain.dto.FileMergeDto;
import com.neurocast.media.domain.dto.FileUploadInitDto;
import com.neurocast.media.domain.vo.FileUploadInitVo;
import com.neurocast.media.domain.vo.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件上传服务：分片上传（断点续传）、秒传、批量上传
 */
public interface FileUploadService {

    /**
     * 初始化分片上传：秒传检测 + 断点续传进度恢复
     */
    FileUploadInitVo initUpload(FileUploadInitDto initDto);

    /**
     * 上传分片
     */
    boolean uploadChunk(String uploadId, Integer chunkIndex, MultipartFile chunkFile) throws IOException;

    /**
     * 合并分片并校验 MD5
     */
    FileUploadVo mergeChunks(FileMergeDto mergeDto) throws IOException;

    /**
     * 批量上传（小文件简单模式）
     */
    List<FileUploadVo> batchUpload(MultipartFile[] files, String subDir) throws IOException;

    /**
     * 查询已上传分片索引
     */
    List<Integer> getUploadedChunks(String uploadId);
}
