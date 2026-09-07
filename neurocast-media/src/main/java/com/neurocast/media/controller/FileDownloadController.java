package com.neurocast.media.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.neurocast.media.service.MediaFileService;

import lombok.RequiredArgsConstructor;

/**
 * 媒体文件下载接口（走 SecurityConfig 白名单，由应用层登录控制访问）
 */
@RestController
@RequestMapping("/api/file/download")
@RequiredArgsConstructor
public class FileDownloadController {

    private final MediaFileService mediaFileService;

    /**
     * 流式下载媒体文件（支持 Range 断点续传）
     */
    @GetMapping("/{fileType}/{deviceUid}/{filename}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String fileType,
                                                          @PathVariable String deviceUid,
                                                          @PathVariable String filename,
                                                          @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return mediaFileService.download(fileType, deviceUid, filename, rangeHeader);
    }
}
