package com.neurocast.media.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.neurocast.framework.event.ExternalEvent;
import com.neurocast.media.domain.dto.DataCameraImageDto;
import com.neurocast.media.domain.dto.DataCameraImageRecordDto;
import com.neurocast.media.domain.dto.DataCameraVideoDto;
import com.neurocast.media.domain.dto.DataFileUploadResultDto;
import com.neurocast.media.service.MediaEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体事件异步监听器。
 * <p>
 * 监听 ExternalEvent，根据 type 字段分发到对应的处理逻辑。
 * 使用 @Async 在独立线程池中执行，不阻塞 HTTP 响应。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaEventListener {

    private static final String TYPE_IMAGE = "media.image";
    private static final String TYPE_IMAGE_BATCH = "media.image.batch";
    private static final String TYPE_VIDEO = "media.video";
    private static final String TYPE_FILE_UPLOAD_RESULT = "media.file_upload_result";

    private final MediaEventService mediaEventService;

    @Async("eventTaskExecutor")
    @EventListener(condition = "#event.type.startsWith('media.')")
    public void onMediaEvent(ExternalEvent event) {
        JSONObject payload = event.getPayload();
        switch (event.getType()) {
            case TYPE_IMAGE -> {
                DataCameraImageDto dto = payload.toJavaObject(DataCameraImageDto.class);
                mediaEventService.createCameraImage(dto);
            }
            case TYPE_IMAGE_BATCH -> {
                DataCameraImageRecordDto dto = payload.toJavaObject(DataCameraImageRecordDto.class);
                mediaEventService.createCameraImageRecord(dto);
            }
            case TYPE_VIDEO -> {
                DataCameraVideoDto dto = payload.toJavaObject(DataCameraVideoDto.class);
                mediaEventService.createCameraVideoRecord(dto);
            }
            case TYPE_FILE_UPLOAD_RESULT -> {
                DataFileUploadResultDto dto = payload.toJavaObject(DataFileUploadResultDto.class);
                mediaEventService.handleFileUploadResult(dto);
            }
            default -> log.debug("媒体模块忽略事件：type={}", event.getType());
        }
    }
}
