package com.neurocast.media.service;

import com.neurocast.media.domain.dto.FilePrepareDto;
import com.neurocast.media.domain.vo.FilePrepareResultVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * 媒体文件服务：请求设备上传文件、服务端文件下载
 */
public interface MediaFileService {

    /**
     * 请求设备上传指定媒体文件（未上传/上传失败时下发上传 RPC）
     */
    FilePrepareResultVo prepareFile(FilePrepareDto dto);

    /**
     * 流式下载媒体文件（支持 Range 断点续传）
     */
    ResponseEntity<StreamingResponseBody> download(String fileType, String deviceUid, String filename,
                                                   String rangeHeader);
}
