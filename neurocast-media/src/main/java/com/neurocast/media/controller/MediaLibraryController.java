package com.neurocast.media.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.core.domain.Result;
import com.neurocast.media.domain.dto.CameraImageSearchQuery;
import com.neurocast.media.domain.dto.CameraVideoSearchQuery;
import com.neurocast.media.domain.dto.FilePrepareDto;
import com.neurocast.media.domain.vo.CameraImageVo;
import com.neurocast.media.domain.vo.CameraVideoVo;
import com.neurocast.media.domain.vo.FilePrepareResultVo;
import com.neurocast.media.service.CameraImageService;
import com.neurocast.media.service.CameraVideoService;
import com.neurocast.media.service.MediaFileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 媒体库接口：抓拍图片/录像记录查询、请求设备上传
 */
@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
public class MediaLibraryController {

    private final CameraImageService cameraImageService;
    private final CameraVideoService cameraVideoService;
    private final MediaFileService mediaFileService;

    /**
     * 抓拍图片分页列表
     */
    @GetMapping("/image/list")
    public Result<PageResult<CameraImageVo>> imageList(CameraImageSearchQuery query) {
        return Result.ok(cameraImageService.page(query));
    }

    /**
     * 抓拍图片详情
     */
    @GetMapping("/image/{id}")
    public Result<CameraImageVo> imageDetail(@PathVariable String id) {
        return Result.ok(cameraImageService.findById(id));
    }

    /**
     * 录像分页列表
     */
    @GetMapping("/video/list")
    public Result<PageResult<CameraVideoVo>> videoList(CameraVideoSearchQuery query) {
        return Result.ok(cameraVideoService.page(query));
    }

    /**
     * 录像详情
     */
    @GetMapping("/video/{id}")
    public Result<CameraVideoVo> videoDetail(@PathVariable String id) {
        return Result.ok(cameraVideoService.findById(id));
    }

    /**
     * 请求设备上传媒体文件（未上传时触发上传，已上传时直接返回下载链接）
     */
    @PostMapping("/file/prepare")
    public Result<FilePrepareResultVo> prepareFile(@Valid @RequestBody FilePrepareDto dto) {
        return Result.ok(mediaFileService.prepareFile(dto));
    }
}
