package com.neurocast.media.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.neurocast.common.core.domain.Result;
import com.neurocast.media.domain.dto.FileMergeDto;
import com.neurocast.media.domain.dto.FileUploadInitDto;
import com.neurocast.media.domain.vo.FileUploadInitVo;
import com.neurocast.media.domain.vo.FileUploadVo;
import com.neurocast.media.service.FileUploadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 文件上传接口：分片上传（断点续传/秒传）与批量小文件上传
 */
@RestController
@RequestMapping("/api/admin/file/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 初始化分片上传：秒传检测 + 断点续传进度恢复
     */
    @PostMapping("/init")
    public Result<FileUploadInitVo> init(@Valid @RequestBody FileUploadInitDto initDto) {
        return Result.ok(fileUploadService.initUpload(initDto));
    }

    /**
     * 上传单个分片
     */
    @PostMapping("/chunk")
    public Result<Boolean> chunk(@RequestParam String uploadId,
                                 @RequestParam Integer chunkIndex,
                                 @RequestParam("file") MultipartFile chunkFile) throws IOException {
        return Result.ok(fileUploadService.uploadChunk(uploadId, chunkIndex, chunkFile));
    }

    /**
     * 合并分片并校验 MD5
     */
    @PostMapping("/merge")
    public Result<FileUploadVo> merge(@Valid @RequestBody FileMergeDto mergeDto) throws IOException {
        return Result.ok(fileUploadService.mergeChunks(mergeDto));
    }

    /**
     * 批量上传（小文件简单模式）
     */
    @PostMapping("/batch")
    public Result<List<FileUploadVo>> batch(@RequestParam("files") MultipartFile[] files,
                                            @RequestParam(required = false) String subDir) throws IOException {
        return Result.ok(fileUploadService.batchUpload(files, subDir));
    }

    /**
     * 查询已上传分片索引（断点续传进度）
     */
    @GetMapping("/progress/{uploadId}")
    public Result<List<Integer>> progress(@PathVariable String uploadId) {
        return Result.ok(fileUploadService.getUploadedChunks(uploadId));
    }
}
